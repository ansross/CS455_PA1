#!/bin/bash

clear

echo "Starting Program"

PROJECT_FILE_PATH="~/cs455/PA1/src"
MSG_NODE_PATH="cs455.overlay.node.MessagingNode"
REG_NODE_PATH="cs455.overlay.node.Registry"
REG_NODE_NAME="montgomery"
REG_NODE_PORT="15008"

cd $PROJECT_FILE_PATH
echo "Project made"
cd $PROJECT_FILE_PATH
java $MSG_NODE_PATH $REG_NODE_NAME $REG_NODE_PORT
echo "Made Node"
