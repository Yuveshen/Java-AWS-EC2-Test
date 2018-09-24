package com.amazonaws.test;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.resources.Credentials;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.*;
import org.testng.annotations.BeforeClass;

public class BaseTestSuite {

    protected AmazonEC2Client ec2Client;
    protected RunInstancesRequest runInstancesRequest;
    protected static BasicAWSCredentials awsCreds;

    @BeforeClass
    // Passing Browser parameter from TestNG xml
    public static void setUpBeforeClass() {
        awsCreds = new BasicAWSCredentials(Credentials.access_key_id, Credentials.secret_access_key);
    }

    public String createNewEC2Instance() {
        ec2Client = new AmazonEC2Client(awsCreds);
        ec2Client.setEndpoint("ec2.us-east-2.amazonaws.com");

        runInstancesRequest = new RunInstancesRequest();
        runInstancesRequest.withImageId("ami-0b59bfac6be064b78")
                .withInstanceType("t2.micro")
                .withKeyName("my-key-pair")
                .withMinCount(1)
                .withMaxCount(1)
                .withSecurityGroups("default");
        return ec2Client.runInstances(runInstancesRequest).getReservation().getInstances().get(0).getInstanceId();
    }

    public void stopEC2Instance(String instanceId) {
        StopInstancesRequest stopInstanceRequest = new StopInstancesRequest().withInstanceIds(instanceId);
        ec2Client.stopInstances(stopInstanceRequest);
    }

    public void terminateEC2Instance(String instanceId) {
        TerminateInstancesRequest terminateInstanceRequest = new TerminateInstancesRequest().withInstanceIds(instanceId);
        ec2Client.terminateInstances(terminateInstanceRequest);
    }


    public Integer getInstanceStatus(String instanceId) {
        DescribeInstancesRequest describeInstanceRequest = new DescribeInstancesRequest().withInstanceIds(instanceId);
        DescribeInstancesResult describeInstanceResult = ec2Client.describeInstances(describeInstanceRequest);
        InstanceState state = describeInstanceResult.getReservations().get(0).getInstances().get(0).getState();
        return state.getCode();
    }

    public void verifyInstanceStatus(String status, String yourInstanceId) {
        int expectedState;
        int instanceState = -1;
        int count = 0;

        switch (status) {
            case "pending":
                expectedState = 0;
                break;
            case "running":
                expectedState = 16;
                break;
            case "shutting-down":
                expectedState = 32;
                break;
            case "terminated":
                expectedState = 48;
                break;
            case "stopping":
                expectedState = 64;
                break;
            case "stopped":
                expectedState = 80;
                break;
            default:
                expectedState = 0;
        }

        while (instanceState != expectedState || count < 12) { //Loop until the instance is in the state or time expires
            instanceState = getInstanceStatus(yourInstanceId);
            try {
                Thread.sleep(5000);
                count++;
            } catch (InterruptedException e) {
                System.out.println("Failed with status : " + instanceState);
            }
        }

    }

}
