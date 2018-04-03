#!/usr/bin/python3
# coding:utf-8
import os
import sys
import re

reg_charset = r'''[\u4E00-\u9FA5]'''
reg_pattern = re.compile(reg_charset)


def is_chinese_comment(line):
    matched_entries = reg_pattern.findall(line)
    match_count = len(matched_entries)
    return match_count > 0


class ReplaceContent(object):
    def __init__(self, line, newcontent):
        self.__line = line
        self.__content = newcontent

    def get_line(self):
        return self.__line

    def get_content(self):
        return self.__content


if __name__ == '__main__':
    if len(sys.argv) < 2:
        print("usage: python3 " + sys.argv[0] + " filePath")
        exit(-1)
    target_file = sys.argv[1]
    print("准备处理文件:", target_file)
    delete_lines = []
    comm_start = False
    comm_end = False
    comm_content = False

    block_to_delete = False
    replace_list = []
    with open(target_file) as source_file:
        line_number = 0
        while True:
            line_all = source_file.readline()
            if len(line_all) == 0:
                break
            line_number += 1

            if line_all.find("*/") != -1 and line_all.find("/*") != -1 and is_chinese_comment(line_all):
                pos_start = line_all.find("/*")
                pos_end = line_all.find("*/")
                comment = line_all[pos_start:pos_end]
                if is_chinese_comment(comment):
                    new_line = line_all[0:pos_start] + line_all[pos_end + 2:]
                    if len(str.strip(new_line)) > 0:
                        replace_list.append(ReplaceContent(line_number, new_line))

            if line_all.find("//") != -1 and is_chinese_comment(line_all):
                pos_start = line_all.find("//")
                comment = line_all[pos_start:]
                print(comment)
                if is_chinese_comment(comment):
                    new_line = str.rstrip(line_all[0:pos_start]) + "\n"
                    if len(str.strip(new_line)) > 0:
                        replace_list.append(ReplaceContent(line_number, new_line))

            line = str.strip(line_all)
            if line.startswith("//") and is_chinese_comment(line):
                delete_lines.append(line_number)
            elif line.startswith("/*"):
                if line.endswith("*/") and is_chinese_comment(line):
                    block_to_delete = True
                elif line.endswith("*/"):
                    continue

                line_block = 0
                while not line.endswith("*/"):
                    line_all = source_file.readline()
                    line_block += 1
                    line = str.strip(line_all)
                    if is_chinese_comment(line):
                        block_to_delete = True
                    if line.endswith("*/"):
                        break

                if block_to_delete:
                    for x in range(line_block + 1):
                        delete_lines.append(line_number + x)
                    block_to_delete = False

                line_number += line_block

    if len(delete_lines) == 0 and len(replace_list) == 0:
        exit(0)

    new_file = open(target_file + ".nolog", "w+")
    with open(target_file) as source_file:
        line_number = 0
        while True:
            line_all = source_file.readline()
            if len(line_all) == 0:
                break
            line_number += 1

            replace_line = False
            for replace in replace_list:
                if replace.get_line() == line_number:
                    new_file.write(replace.get_content())
                    replace_line = True
                    break

            if line_number not in delete_lines and not replace_line:
                new_file.write(line_all)

    new_file.close()
    os.remove(target_file)
    os.rename(target_file + ".nolog", target_file)
