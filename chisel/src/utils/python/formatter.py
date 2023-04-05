import sys


def get_rightmost_equal(lines):
    rightmost = -1
    for line in lines:
        pos = max(line.find('='), line.find(':='))
        if pos > rightmost:
            rightmost = pos
    return rightmost


def align_equals(lines):
    rightmost = get_rightmost_equal(lines)

    for i, line in enumerate(lines):
        pos = max(line.find('='), line.find(':='))
        if pos != -1:
            padding = rightmost - pos
            space = ' ' * padding
            lines[i] = line[:pos] + space + line[pos:]
    
    return lines


def process_file(file_name):
    with open(file_name, 'r') as f:
        lines = f.readlines()
    
    aligned_lines = align_equals(lines)

    try:
        with open(file_name, "w") as f:
            f.write("".join(aligned_lines))
    except IOError:
        print(f"文件{file_name}写入失败")


if __name__ == '__main__':
    file_name = sys.argv[1]
    process_file(file_name)
