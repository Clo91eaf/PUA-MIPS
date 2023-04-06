import argparse
import re

parser = argparse.ArgumentParser(description='auto add comment in the io field.')
parser.add_argument('-f', '--input_file', required=True, help='Input file name')
parser.add_argument('-o', '--output_file', required=True, help='Output file name')
args = parser.parse_args()

with open(args.input_file, 'r') as f:
    lines = f.readlines()

code = None
for i in range(len(lines)):
    line = lines[i].strip()

    if (':=' in line and 'io.' in line) or 'io-finish' in line:
        if 'io-finish' in line:
            break
        new_code = line.split('.')[1].replace('from', '').strip()
        new_code = re.sub(r'(?<!^)(?=[A-Z])', ' ', new_code).lower()
        io_type = 'output' if line.index('io.') < line.index(':=') else 'input'
        if code != new_code:
            code = new_code
            if not lines[i-1].strip().startswith('//'):
                lines.insert(i, f'\n  // {io_type}-{code}\n')

with open(args.output_file, 'w') as f:
    f.write(''.join(lines))
