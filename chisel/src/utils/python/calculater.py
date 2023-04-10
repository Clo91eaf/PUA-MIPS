import argparse
import re

def calculate(expression):
    # 替换所有的16进制数为10进制数
    expression = re.sub(r'0[xX]([0-9a-fA-F]+)', lambda m: str(int(m.group(1), 16)), expression)
    # 计算括号内的表达式
    while '(' in expression:
        expression = re.sub(r'\(([^()]+)\)', lambda m: str(calculate(m.group(1))), expression)
    # 处理乘法和除法
    expression = re.sub(r'(\d+(\.\d+)?)\s*([*/])\s*(\d+(\.\d+)?)', lambda m: str(eval(m.group(0))), expression)
    # 处理加法和减法
    return str(eval(expression))

if __name__ == '__main__':
    # 创建命令行参数解析器
    parser = argparse.ArgumentParser(description='A simple calculator that supports parentheses, hex, octal and decimal numbers, and basic arithmetic operations')
    parser.add_argument('expression', type=str, help='the expression to calculate')
    # 解析命令行参数
    args = parser.parse_args()
    # 计算表达式并输出结果
    result = calculate(args.expression)
    print(result)
