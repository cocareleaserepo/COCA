# RPC Bridging

This tool help patch the missing client-server RPC call in the typical static analysis, with the source code and compiled version of the project, you can get a patched call graph. 

**Requirement**

+ Python 3.x 
+ JDK 17 
+ libprotoc 27.1

Usage:
```angular2html
Python main.py [-h] --bin_project_dir BIN_PROJECT_DIR --src_project_dir SRC_PROJECT_DIR

Generate unified call graph patched with RPC invocation.

optional arguments:
  -h, --help            show this help message and exit
  --bin_project_dir BIN_PROJECT_DIR
                        The binary directory of the project, your project should be in
                        complied format(i.e., .class files).
  --src_project_dir SRC_PROJECT_DIR
                        The source code directory of the project, your project should be in
                        source code format(i.e., .java files).
```

