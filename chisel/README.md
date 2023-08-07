# PUA-MIPS
===============

# 构建
本Chisel项目使用sbt进行构建.
sbt版本为1.8.2
scala版本为2.13.8
Chisel版本为3.5.4

在Chisel目录下执行
```shell
make verilog
```

执行完成即可在chisel/generated目录下生成PuaMips.v verilog文件.
chisel/src/main/resources/mycpu_top.v可作为顶层文件.

# 引用说明
我们的Cache设计在设计初期借鉴了重庆大学#[NSCSCC2022 CDIM](https://github.com/Maxpicca-Li/CDIM/)的设计思路, 在此表示感谢. 
引用已在代码
  chisel/src/main/cache/DCache
  chisel/src/main/cache/ICache
文件顶部注明.