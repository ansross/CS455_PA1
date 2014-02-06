#!/bin/bash
spawn ssh frankfort
send -- "cd ~/cs455/PA1/src\r"
expect "$"
send -- "bash Registry.sh\r"
interact
