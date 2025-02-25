/* Copyright (c) 2019 FIRST. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted (subject to the limitations in the disclaimer below) provided that
 * the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list
 * of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this
 * list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 *
 * Neither the name of FIRST nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 *
 * NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
 * LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.firstinspires.ftc.teamcode.autonomous;

import android.util.Size;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.geometry.Vector2d;
import com.acmerobotics.roadrunner.trajectory.Trajectory;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;

import org.firstinspires.ftc.robotcore.external.hardware.camera.BuiltinCameraDirection;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.hardware.camera.controls.ExposureControl;
import org.firstinspires.ftc.robotcore.external.hardware.camera.controls.GainControl;
import org.firstinspires.ftc.robotcore.external.tfod.Recognition;
import org.firstinspires.ftc.teamcode.RobotHardware;
import org.firstinspires.ftc.teamcode.drive.SampleMecanumDrive;
import org.firstinspires.ftc.teamcode.trajectorysequence.TrajectorySequence;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.tfod.TfodProcessor;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

/**
 * This 2023-2024 OpMode illustrates the basics of TensorFlow Object Detection,
 * including Java Builder structures for specifying Vision parameters.
 *
 * Use Android Studio to Copy this Class, and Paste it into your team's code folder with a new name.
 * Remove or comment out the @Disabled line to add this OpMode to the Driver Station OpMode list.
 */
@Autonomous(name = "nonCanvasBlueTeamAuto", group = "Concept")

public class nonCanvasBlueTeamAuto extends LinearOpMode {

    private static final boolean USE_WEBCAM = true;  // true for webcam, false for phone camera

    /**
     * {@link #tfod} is the variable to store our instance of the TensorFlow Object Detection processor.
     */
    private TfodProcessor tfod;

    /**
     * {@link #visionPortal} is the variable to store our instance of the vision portal.
     */
    private VisionPortal visionPortal;

    @Override
    public void runOpMode() {

        initTfod();
        RobotHardware robot = new RobotHardware();
        robot.init(hardwareMap);

        // ViperSlide ain't used. just extra code
        DcMotor ViperSlide;
        DcMotor ViperSlide2;

        ViperSlide = hardwareMap.get(DcMotor.class, "ViperSlide");
        ViperSlide2 = hardwareMap.get(DcMotor.class, "ViperSlide2");

        ViperSlide.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        ViperSlide2.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        ViperSlide.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        ViperSlide2.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        ViperSlide.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        ViperSlide2.setMode(DcMotor.RunMode.RUN_TO_POSITION);

        sleep(2500);

        // Wait for the DS start button to be touched.
        telemetry.addData("DS preview on/off", "3 dots, Camera Stream");
        telemetry.addData(">", "Touch Play to start OpMode");
        telemetry.update();

        ExposureControl exposureControl;
        GainControl gainControl;
        String cubePosition = "";

        while(!opModeIsActive()){
            if(isStopRequested()){
                visionPortal.close();
            }
            telemetryTfod();

            // Push telemetry to the Driver Station.
            telemetry.update();

            // Save CPU resources; can resume streaming when needed.
            if (gamepad1.dpad_down) {
                visionPortal.stopStreaming();
            } else if (gamepad1.dpad_up) {
                visionPortal.resumeStreaming();
            }
            exposureControl = visionPortal.getCameraControl(ExposureControl.class);
            //exposureControl.setMode(ExposureControl.Mode.ContinuousAuto); // prev continuousAuto
            exposureControl.setMode(ExposureControl.Mode.Manual);
            exposureControl.setExposure((long) 4, TimeUnit.MILLISECONDS);


            gainControl = visionPortal.getCameraControl(GainControl.class);
            gainControl.setGain(255);

            exposureControl.setExposure((long) 0, TimeUnit.MILLISECONDS); //prev 655


                /*telemetry.addData("exposure min: ", exposureControl.getMinExposure(TimeUnit.SECONDS));
                telemetry.addData("exposure max: ", exposureControl.getMaxExposure(TimeUnit.SECONDS));
                telemetry.addData("exposiure :", exposureControl.getExposure(TimeUnit.SECONDS));
                telemetry.update();*/

            // Share the CPU.
            sleep(20);

            List<Recognition> currentRecognitions = tfod.getRecognitions();

            // Step through the list of recognitions and display info for each one.
            for (Recognition recognition : currentRecognitions) {
                double x = (recognition.getLeft() + recognition.getRight()) / 2;
                double y = (recognition.getTop() + recognition.getBottom()) / 2;

                if (0 < x && x < 400) {
                    cubePosition = "left";
                } else if (500 < x && x < 900) {
                    cubePosition = "center";
                } else if (1000 < x && x < 1280) {
                    cubePosition = "right";
                }
            }

        }

        //important: code copied from canvasBlueTeamAuto

        SampleMecanumDrive drive = new SampleMecanumDrive(hardwareMap);

        Trajectory temp = null;

        ArrayList<Trajectory> lotsOfMovement = new ArrayList<>();
//        robot.servo3.setPower(-0.1);
//
//        if(cubePosition.equals("left")){ // old right side code
//            temp = drive.trajectoryBuilder(new Pose2d())
//                    .splineTo(new Vector2d(20,-5),Math.toRadians(0))
//                    .splineToSplineHeading(new Pose2d(20,2, Math.toRadians(45)), Math.toRadians(90))
//                    .build();
//            lotsOfMovement.add(temp);
//
//        }
//
//        if (cubePosition.equals("center")) {
//
//            temp = drive.trajectoryBuilder(new Pose2d())
//                    .lineToConstantHeading(new Vector2d(25,0))
//                    .build();
//            lotsOfMovement.add(temp);
//
//
//        }
//
//        if (cubePosition.equals("right")) { // old left side code
//
//            temp = drive.trajectoryBuilder(new Pose2d())
//                    .splineToLinearHeading(new Pose2d(20,-9, Math.toRadians(0)), Math.toRadians(-0))
//                    .build();
//            lotsOfMovement.add(temp);
//
//            // armature should move down after this
//        }
//
//
//        for(Trajectory trajectory:lotsOfMovement){
//            drive.followTrajectory(trajectory);
//            sleep(1000);
//        }
//        robot.spaghettiIntake.setPower(0.5);
//        robot.servo1.setPower(-1);
//        robot.servo3.setPower(-1);
//
//        sleep(1000);
//
//        robot.servo3.setPower(1);
//
//        sleep(1000);
//        robot.spaghettiIntake.setPower(0);
//        robot.servo1.setPower(0);


        if(cubePosition.equals("left")){
            TrajectorySequence untitled0 = drive.trajectorySequenceBuilder(new Pose2d(-36.60, 65.37, Math.toRadians(-90.00)))
                    .splineToSplineHeading(new Pose2d(-30.25, 40.83, Math.toRadians(-47.73)), Math.toRadians(-47.73))
                    .splineToSplineHeading(new Pose2d(-27.86, 13.19, Math.toRadians(-1.27)), Math.toRadians(-1.27))
                    .splineToSplineHeading(new Pose2d(45.63, 30.53, Math.toRadians(82.96)), Math.toRadians(82.96))
                    .splineToSplineHeading(new Pose2d(49.57, 37.16, Math.toRadians(-12.09)), Math.toRadians(-12.09))
                    .build();
            drive.setPoseEstimate(untitled0.start());
            drive.followTrajectorySequence(untitled0);
        }

        if(cubePosition.equals("center")){
            TrajectorySequence untitled0 = drive.trajectorySequenceBuilder(new Pose2d(-36.60, 65.37, Math.toRadians(-90.00)))
                    .splineToSplineHeading(new Pose2d(-35.61, 29.69, Math.toRadians(-90.00)), Math.toRadians(-90.00))
                    .lineToSplineHeading(new Pose2d(-50.70, 27.43, Math.toRadians(-1.27)))
                    .splineTo(new Vector2d(-37.73, 13.05), Math.toRadians(18.38))
                    .splineToSplineHeading(new Pose2d(45.63, 30.53, Math.toRadians(82.96)), Math.toRadians(82.96))
                    .splineToSplineHeading(new Pose2d(49.57, 37.16, Math.toRadians(-12.09)), Math.toRadians(-12.09))
                    .build();
            drive.setPoseEstimate(untitled0.start());
            drive.followTrajectorySequence(untitled0);
        }

        if(cubePosition.equals("right")){
            TrajectorySequence untitled0 = drive.trajectorySequenceBuilder(new Pose2d(-36.60, 65.37, Math.toRadians(-90.00)))
                    .splineToSplineHeading(new Pose2d(-47.04, 39.28, Math.toRadians(-90.00)), Math.toRadians(-90.00))
                    .lineToSplineHeading(new Pose2d(-57.76, 30.53, Math.toRadians(-1.27)))
                    .splineTo(new Vector2d(-37.73, 13.05), Math.toRadians(18.38))
                    .splineToSplineHeading(new Pose2d(45.63, 30.53, Math.toRadians(82.96)), Math.toRadians(82.96))
                    .splineToSplineHeading(new Pose2d(49.57, 37.16, Math.toRadians(-12.09)), Math.toRadians(-12.09))
                    .build();
            drive.setPoseEstimate(untitled0.start());
            drive.followTrajectorySequence(untitled0);
        }

        // Save more CPU resources when camera is no longer needed.
        visionPortal.close();

    }   // end runOpMode()

    /**
     * Initialize the TensorFlow Object Detection processor.
     */
    private void initTfod() {

        // Create the TensorFlow processor by using a builder.
        tfod = new TfodProcessor.Builder()

                // Use setModelAssetName() if the TF Model is built in as an asset.

                //.setModelFileName("object_test1.tflite")

                //.setModelFileName("red_cube_v1_model_20231026_113436.tflite")
                .setModelFileName("object_test2.tflite")

                //setModelFileName("object_test2.tflite")


                // Use setModelFileName() if you have downloaded a custom team model to the Robot Controller.
                //.setModelAssetName(TFOD_MODEL_ASSET)
                //.setModelFileName(TFOD_MODEL_FILE)


                //.setModelLabels(LABELS)

                //.setIsModelTensorFlow2(true)
                //.setIsModelQuantized(true)
                //.setModelInputSize(300)
                //.setModelAspectRatio(16.0 / 9.0)


                .build();


        WebcamName cam = hardwareMap.get(WebcamName.class, "Webcam 1");


        //Camera webcam = hardwareMap.get(Camera.class, "Webcam 1");


        //FtcDashboard.getInstance().startCameraStream(,60);

        // Create the vision portal by using a builder.
        VisionPortal.Builder builder = new VisionPortal.Builder();


        // Set the camera (webcam vs. built-in RC phone camera).
        if (USE_WEBCAM) {
            builder.setCamera(hardwareMap.get(WebcamName.class, "Webcam 1"));
        } else {
            builder.setCamera(BuiltinCameraDirection.BACK);
        }

        // Choose a camera resolution. Not all cameras support all resolutions.
        //builder.setCameraResolution(new Size(640, 480));
        builder.setCameraResolution(new Size(1280, 720));

        // Enable the RC preview (LiveView).  Set "false" to omit camera monitoring.
        //builder.enableCameraMonitoring(true);
        //builder.enableCameraMonitoring(true);

        // Set the stream format; MJPEG uses less bandwidth than default YUY2.
        //builder.setStreamFormat(VisionPortal.StreamFormat.YUY2);

        // Choose whether or not LiveView stops if no processors are enabled.
        // If set "true", monitor shows solid orange screen if no processors enabled.
        // If set "false", monitor shows camera view without annotations.
        //builder.setAutoStopLiveView(false);

        // Set and enable the processor.
        builder.addProcessor(tfod);

        // Build the Vision Portal, using the above settings.
        visionPortal = builder.build();

        // Set confidence threshold for TFOD recognitions, at any time.
        //tfod.setMinResultConfidence(0.75f);
        tfod.setMinResultConfidence(0.3f);

        // Disable or re-enable the TFOD processor at any time.
        //visionPortal.setProcessorEnabled(tfod, true);

    }   // end method initTfod()

    /**
     * Function to add telemetry about TensorFlow Object Detection (TFOD) recognitions.
     */
    private void telemetryTfod() {
        double highestConf = 0;
        List<Recognition> currentRecognitions = tfod.getRecognitions();
        Recognition recognition = null;
        telemetry.addData("# Objects Detected", currentRecognitions.size());

        for(int i = 0; i<currentRecognitions.size(); i++){
            if(currentRecognitions.get(i).getConfidence() > highestConf){
                recognition = currentRecognitions.get(i);
                highestConf = recognition.getConfidence();

            }
        }

        // Step through the list of recognitions and display info for each one.
        if(recognition != null){
            double x = (recognition.getLeft() + recognition.getRight()) / 2 ;
            double y = (recognition.getTop()  + recognition.getBottom()) / 2 ;

            if(0 < x && x < 400){
                telemetry.addData("Cube Pos: ", "left");
            }
            else if(500 < x && x < 900){
                telemetry.addData("Cube Pos: ", "center");
            }
            else if(1000 < x && x < 1280){
                telemetry.addData("Cube Pos: ", "right");
            }

            telemetry.addData(""," ");
            telemetry.addData("Image", "%s (%.0f %% Conf.)", recognition.getLabel(), recognition.getConfidence() * 100);
            telemetry.addData("- Position", "%.0f / %.0f", x, y);
            telemetry.addData("- Size", "%.0f x %.0f", recognition.getWidth(), recognition.getHeight());
            }


    }   // end method telemetryTfod()



}   // end class
