package com.amazonaws.test;

import org.testng.annotations.Test;

public class CreateInstanceTest extends BaseTestSuite {

    @Test
    public void createEC2InstanceTest() throws Exception {

        String instanceId = createNewEC2Instance();
        verifyInstanceStatus("running", instanceId);

        stopEC2Instance(instanceId);
        terminateEC2Instance(instanceId);
        verifyInstanceStatus("terminated", instanceId);

    }


}

