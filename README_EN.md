[简体中文](./README.md) | English

# 🚀 PUA (Powerful Ultimate Architecture) MIPS 🏗️
## 🛠️ Environment Setup

This Chisel project is built using sbt.
- sbt version: 1.8.2
- scala version: 2.13.8
- Chisel version: 3.5.4

Execute the following command in the chisel directory to generate the Verilog file:

```shell
make verilog
```

Upon completion, you will find the `PuaMips.v` Verilog file in the `chisel/generated` directory, and `chisel/src/main/resources/mycpu_top.v` can be used as the top-level file.

## 📚 Introduction

This project is based on "CPU Design in Practice" and features a simple high-performance dual-issue six-stage pipelined MIPS processor. In the preliminary round, it achieved a performance score of 62, a frequency of 88MHz, and an IPC of 1.25.
For specific design details, please refer to `doc/final-design.pdf`.
This processor is capable of running the latest version of the Linux 6.5-rc3 kernel.
The codebase for this processor does not exceed 5000 lines, making it one of the smallest processors in the history of the Longson Cup.
There is significant room for improvement in this processor, and we welcome you to explore it. Due to time constraints, we did not focus extensively on optimizing the frequency.
This project is in its final stages and will no longer receive maintenance.

## 📦 Resources

1. [Git Commit Message Conventions](https://gitee.com/help/articles/4231#article-header0) 📜 - Git's commit conventions.
2. [Chisel-template](https://github.com/freechipsproject/chisel-template) 📁 - The Chisel project in pua-mips was initialized using this template.
3. [Online Assembler](https://godbolt.org/) 💻 - An online compiler used to generate corresponding MIPS binary files for reference.
4. [MIPS Converter](https://www.eg.bucknell.edu/~csci320/mips_web/) 🔄 - Responsible for converting MIPS instructions into binary and providing detailed explanations of the instructions.
5. [Chisel Environment Setup](https://clo91eaf.github.io/80b5fe4ebe03/) 🛠️

## 🧩 Miscellaneous

### Proper Ways to Reference This Project

1. ❌ Please do not directly copy the code from this project.
2. ✍️ After understanding the modules of this project, implement the code in your own coding style. Try to achieve better code quality through the following aspects:
   - 💬 Code readability with ample comments.
   - 🧰 Code maintainability through decoupling and modularization. In this project, except for a few files, the code size of other files is within 200 lines.
   - 🚀 Utilize the high-level abstraction capabilities of the Chisel language.
   - 📝 Discard software thinking; design first, then implement.
3. 📊 Compare performance through performance counters. Optimization should always be data-driven.
4. 📡 Use simulators and emulators. Avoid falling into the waveform trap. Don't settle for the status quo. Always think about how to minimize waveform viewing and gain more insights.

### Some Characteristics of This Project

In this project, you will inevitably encounter:

1. 📝 Mixed comments in both Chinese and English.
2. 🎨 Inconsistent code styles.
3. 🤔 Odd naming conventions.
4. 🧱 Unexplained hardcoded designs.
5. 🔄 Redundant commit records.

While we have made efforts to address these issues during development and have conducted multiple refactorings, we cannot guarantee code perfection due to time constraints.

If our design process inspires you, we would be delighted. 🌟