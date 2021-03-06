package com.robot.et.core.software.ros;

import android.util.Log;

import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.Node;
import org.ros.node.topic.Publisher;
import org.ros.node.topic.Subscriber;

import java.util.Timer;
import java.util.TimerTask;

import geometry_msgs.Twist;

public class MoveControler extends AbstractNodeMain implements MessageListener<nav_msgs.Odometry> {
    // We need a Publisher -- this will only accept data of type Twist,
    // using the <Generic> type mechanism. (Twists are a type of built-in
    // ROS message which can contain movement commands).
    private Publisher<Twist> publisher;

    // We also need a Subscriber to listen to messages of type Odometry.
    private Subscriber<nav_msgs.Odometry> subscriber;

    private Twist currentVelocityCommand;

    private Timer publisherTimer;
    /**
     * currentOrientation The orientation of the robot in degrees.
     */
    private volatile float currentOrientation;
    /**
     * Velocity commands are published when this is true. Not published otherwise.
     * This is to prevent spamming velocity commands.
     */
    private volatile boolean publishVelocity =false;
    //是否向前
    private volatile boolean isForward =false;
    //是否向后
    private volatile boolean isBackWard =false;
    //是否左转
    private volatile boolean isTurnLeft =false;
    //是否右转
    private volatile boolean isTurnRight =false;

    public double degree;

    public double heading;

    // Extending AbstractNodeMain requires you to implement a couple of methods.
    // You should give your ROS Node a meaningful name here.
    @Override
    public GraphName getDefaultNodeName() {
        return GraphName.of("RobotET/core_mover");
    }

    // When the Node starts up, this method will be executed.
    @Override
    public void onStart(ConnectedNode connectedNode) {

        // Create a Publisher for Twist messages on topic "/cmd_vel_mux/input/teleop"
        publisher = connectedNode.newPublisher("/cmd_vel_mux/input/teleop", Twist._TYPE);
        // Create a Twist message using the Publisher
        currentVelocityCommand = publisher.newMessage();
        subscriber = connectedNode.newSubscriber("odom", nav_msgs.Odometry._TYPE);
        subscriber.addMessageListener(this);
        publisherTimer = new Timer();
        publisherTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (publishVelocity) {
                    if (isForward){
                        Log.i("ROS_MOVE","前进");
                        publishVelocity(1,0,0);
                    }else if (isBackWard){
                        Log.i("ROS_MOVE","后退");
                        publishVelocity(-1,0,0);
                    }else if (isTurnLeft){
                        Log.i("ROS_MOVE","向左");
                        publishVelocity(0,0,1);
                    }else if (isTurnRight){
                        Log.i("ROS_MOVE","向右");
                        publishVelocity(0,0,-1);
                    }else {
                        Log.i("ROS_MOVE","停止");
                        publishVelocity(0,0,0);
                    }
                    publisher.publish(currentVelocityCommand);
                }
            }
        }, 0, 80);
    }

    @Override
    public void onNewMessage(final nav_msgs.Odometry message) {
        double w = message.getPose().getPose().getOrientation().getW();
        double x = message.getPose().getPose().getOrientation().getX();
        double y = message.getPose().getPose().getOrientation().getZ();
        double z = message.getPose().getPose().getOrientation().getY();
        heading = Math.atan2(2 * y * w - 2 * x * z, x * x - y * y - z * z + w * w) * 180 / Math.PI;
        currentOrientation = (float) -heading;
        //第一种计算方案
        if (Math.abs(currentOrientation-degree)<20){
            publishVelocity=false;
            Log.i("ROS_STOP_DEGREE","ROS_STOP_DEGREE:"+currentOrientation);
        }

    }
    /**
     * Publish the velocity as a ROS Twist message.
     *
     * @param linearVelocityX
     *          The normalized linear velocity (-1 to 1).
     * @param angularVelocityZ
     *          The normalized angular velocity (-1 to 1).
     */
    private void publishVelocity(double linearVelocityX, double linearVelocityY, double angularVelocityZ) {
        currentVelocityCommand.getLinear().setX(linearVelocityX);
        currentVelocityCommand.getLinear().setY(-linearVelocityY);
        currentVelocityCommand.getLinear().setZ(0);
        currentVelocityCommand.getAngular().setX(0);
        currentVelocityCommand.getAngular().setY(0);
        currentVelocityCommand.getAngular().setZ(angularVelocityZ);
    }

    public void isPublishVelocity(boolean publishVelocity){
        this.publishVelocity=publishVelocity;
    }

    public void execMoveForword(){
       this.isForward=true;
        this.isBackWard=false;
        this.isTurnLeft=false;
        this.isTurnRight=false;
    }
    public void execMoveBackForward(){
        this.isForward=false;
        this.isBackWard=true;
        this.isTurnLeft=false;
        this.isTurnRight=false;
    }
    public void execTurnLeft(){
        this.isForward=false;
        this.isBackWard=false;
        this.isTurnLeft=true;
        this.isTurnRight=false;
    }
    public void execTurnRight(){
        this.isForward=false;
        this.isBackWard=false;
        this.isTurnLeft=false;
        this.isTurnRight=true;
    }
    public void execStop(){
        this.isForward=false;
        this.isBackWard=false;
        this.isTurnLeft=false;
        this.isTurnRight=false;
    }

    public double getCurrentDegree(){
        return currentOrientation;
    }

    public void setDegree(double degree){
        this.degree=degree;
    }

    @Override
    public void onShutdown(Node node) {
    }

    @Override
    public void onShutdownComplete(Node node) {
        publisherTimer.cancel();
        publisherTimer.purge();
    }

    @Override
    public void onError(Node node, Throwable throwable) {
    }
}

