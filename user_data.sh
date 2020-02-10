#!/bin/sh

VERSION="1.1.0"
DOWNLOAD_URL="https://github.com/xxlabaza/future-service/releases/download/${VERSION}/future-service-${VERSION}.jar"

AWS_ACCESS_KEY=""
AWS_SECRET_KEY=""
AWS_REGION=`echo "${HOSTNAME}" | cut -d '.' -f 2`
AWS_ELB_NAME=""

APP_PROPERTIES=""
APP_PROPERTIES+="--cluster.aws.enabled=true "
APP_PROPERTIES+="--cluster.aws.key.access=${AWS_ACCESS_KEY} "
APP_PROPERTIES+="--cluster.aws.key.access=${AWS_SECRET_KEY} "
APP_PROPERTIES+="--cluster.aws.elb.name=${AWS_REGION} "
APP_PROPERTIES+="--cluster.aws.elb.region=${AWS_ELB_NAME}"

COMMAND="/usr/bin/nohup /usr/bin/java -jar /home/ec2-user/app.jar ${APP_PROPERTIES} > /home/ec2-user/output.log &"

yum update --assumeyes
yum install --assumeyes java-11-amazon-corretto-headless.x86_64
wget --output-document=/home/ec2-user/app.jar "${DOWNLOAD_URL}"

echo "@reboot ${COMMAND}" >> /var/spool/cron/ec2-user
su - ec2-user -c "${COMMAND}"
