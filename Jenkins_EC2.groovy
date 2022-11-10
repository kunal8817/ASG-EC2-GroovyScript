import groovy.json.JsonSlurperClassic
import groovy.json.JsonSlurper
import groovy.json.JsonOutput


def jsonData
def jsonLength
def currentAsg
def currentEC2

node('built-in') {
    properties(
    [
        // other properties that you have
        pipelineTriggers([cron('*/2 * * * *')]),
    ]
)
    stage('Test') {
        deleteDir()
        existing_asg = sh (script: "aws autoscaling describe-auto-scaling-groups --query \"AutoScalingGroups[? Tags[?Key=='Application_name' && Value=='IPS']].[AutoScalingGroupName]\" --output json", returnStdout: true).trim()
        echo "existing_asg: ${existing_asg}"
        def idList = new JsonSlurper().parseText(existing_asg)
        jsonLength = idList.size()
        echo"${jsonLength}"
        for(int i=0;i<jsonLength;i++){
            currentAsg = idList[i][0];
            echo"${currentAsg}"
		    sh (script: "aws autoscaling suspend-processes --auto-scaling-group-name \"${currentAsg}\" --scaling-processes Terminate", returnStdout: true).trim()
        }
        //sh (script: "aws autoscaling suspend-processes --auto-scaling-group-name \"${currentAsg}\" --scaling-processes Terminate", returnStdout: true).trim()
    }
    stage('EC2 Instance') {
        deleteDir()
        existing_ec2 = sh (script: "aws ec2 describe-instances --filters \"Name=tag:Application_name,Values=IPS\" --query 'Reservations[*].Instances[*].InstanceId' --output json", returnStdout: true).trim()
        echo "existing_ec2: ${existing_ec2}"
        def idList = new JsonSlurper().parseText(existing_ec2)
        jsonLength = idList.size()
        echo"${jsonLength}"
        for(int j=0;j<jsonLength;j++){
            currentEC2 = idList[j][0];
            echo "Deleting Corrosponding EC2 Instances ${currentEC2}"
            sh (script: "aws ec2 stop-instances --instance-ids \"${currentEC2}\"", returnStdout: true).trim()
            }
}
    stage('Verification') {
        deleteDir()
        existing_ec2_left = sh (script: "aws ec2 describe-instances --filters \"Name=tag:Application_name,Values=IPS\" --query 'Reservations[*].Instances[*].InstanceId' --output text", returnStdout: true).trim()
        echo "existing_ec2_left: ${existing_ec2_left}"
        /*while(existing_ec2_left != "") {
            existing_ec2_left = sh (script: "aws ec2 describe-instances --filters \"Name=tag:Application_name,Values=IPS\" --query 'Reservations[*].Instances[*].InstanceId' --output text", returnStdout: true).trim()
        }
        echo "existing_ec2_left: ${existing_ec2_left}"*/
    }
}