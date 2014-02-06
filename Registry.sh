#!/bin/bash

REG_PORT_NUMBER="15009"

clear
"make"
java cs455.overlay.node.Registry $REG_PORT_NUMBER
