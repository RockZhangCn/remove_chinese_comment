#!/bin/bash

if [ $# == 0 ] 
then
  echo "./remove_chinese_char.sh file_list"
  return
fi

cat $1 | while read line   #filename 为需要读取的文件名,也可以放在命令行参数里。
do
    python3 remove_chinese_char.py $line
done

