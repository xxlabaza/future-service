#!/bin/sh

VERSION="1.0.0"
DOWNLOAD_URL="https://github.com/xxlabaza/future-service/releases/download/${VERSION}/future-service-${VERSION}.jar"

yum update --assumeyes
yum install --assumeyes java-11-amazon-corretto-headless.x86_64
wget --output-document=/home/ec2-user/app.jar "${DOWNLOAD_URL}"
su - ec2-user -c 'nohup java -jar /home/ec2-user/app.jar > /home/ec2-user/output.log &'
