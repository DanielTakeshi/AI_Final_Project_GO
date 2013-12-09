import os.path,subprocess
from subprocess import STDOUT,PIPE

def compile_java(java_file):

    subprocess.check_call(['javac *.java'])

def execute_java(java_file, stdin):
    
    kami_class, ext = os.path.splitext(java_file)

    cmd = ['java', kami_class]

    proc = subprocess.Popen(cmd, stdin=PIPE, stdout=PIPE, stderr=STDOUT)
    stdout,stderr = proc.communicate(stdin)
    print ('This was "' + stdout + '"')

execute_java('Hi.java', 'Jon')
