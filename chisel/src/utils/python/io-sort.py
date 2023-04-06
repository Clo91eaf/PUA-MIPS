import argparse
import re

parser = argparse.ArgumentParser(description='auto add comment in the io field.')
parser.add_argument('-f', '--input_file', required=True, help='Input file name')
parser.add_argument('-o', '--output_file', required=True, help='Output file name')
args = parser.parse_args()

with open(args.input_file, 'r') as f:
    lines = f.readlines()

input_output_lines = []
output_found = False
for line in lines:
    if "// input\n" in line:
        input_output_lines.append(line)
        output_found = True
    elif "// output\n" in line:
        output_found = False
        input_output_lines.append(line)
        break
    elif output_found:
        input_output_lines.append(line)

# 对 "//input" 到 "//output" 之间的部分进行排序
sorted_lines = sorted(input_output_lines[1:-1])

with open(args.output_file, 'w') as f:
  f.writelines(lines[:lines.index(input_output_lines[0])])
    # 将排序后的 "//input" 到 "//output" 之间的部分写入到输出文件中
  for line in sorted_lines:
      f.write(line)
  f.writelines(lines[lines.index(input_output_lines[-1])+1:])