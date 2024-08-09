"""
# File       :buildCG.py
# version    :python 3.9
# Description：
"""
import os


def build_all_cg(jar_path, tmp_dir, call_graph_dir):
    '''
    Build call graph for all the jars under jar_path
    :param jar_path:
    :param tmp_dir:
    :param call_graph_dir:
    :return:
    '''
    all_jars = os.listdir(jar_path)
    for jar in all_jars:
        if os.path.exists(os.path.join(call_graph_dir, jar + ".txt")):
            continue
        get_call_graph(os.path.join(jar_path, jar), tmp_dir, call_graph_dir)


def get_call_graph(tgt_jar_path, tmp_dir, call_graph_dir):
    '''
    Use Javacg to build call graph
    :param tgt_jar_path:
    :param tmp_dir:
    :param call_graph_dir:
    :return:
    '''
    client = tgt_jar_path.split("/")[-1]

    if os.path.exists(os.path.join(call_graph_dir, client + ".txt")):
        return

    cpcmd = "cp " + tgt_jar_path + " " + tmp_dir
    os.system(cpcmd)
    cmd = "java -classpath " + tmp_dir + " -jar ./lib/javacg-0.1-SNAPSHOT-static.jar" + " " + tgt_jar_path
    output = os.popen(cmd).read()
    if not output or output == "":
        return
    f = open(os.path.join(call_graph_dir, client + ".txt"), "w")
    f.write(output)
    f.close()
    rmcmd = "rm " + tmp_dir + "/" + client
    os.system(rmcmd)