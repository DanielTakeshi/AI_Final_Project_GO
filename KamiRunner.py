#! /usr/bin/env python
import os.path,subprocess, sys
from subprocess import STDOUT,PIPE,Popen
from threading  import Thread
from Queue import Queue, Empty

def enqueue_output(outpipe, queue, directionstring):
    # for every line coming in from the standard out, we put it in the input queue
    # outpipe.readline blocks on the readline until the pipe is closed.

    for line in iter(outpipe.readline, b''):
        print directionstring, " wrote ", line
        queue.put(line)

    # no more input from stdout -- logical end of the pipe reached
    outpipe.close()

def dequeue_output(inpipe, queue, directionstring):
    # read from the queue always, and then push into the input

    while (True):
        if (inpipe.closed):
            break
        try:  line = queue.get(timeout=.1)
        except Empty:
            # nothing on the queue -- wait until next time
            pass
        else: # got line
            print directionstring, " wrote ", line 
            inpipe.write(line + "\n")
            
def run_game(backpropFileName):
    # run the game -- open up fuego
    fuegoCmd = ['fuego']
    fuegoProc = subprocess.Popen(fuegoCmd, stdin=PIPE, stdout=PIPE, bufsize=1, stderr=STDOUT)
    
    # open up our game
    kamiCmd = ['java', 'KamiGo', '-d', backpropFileName]
    kamiProc = subprocess.Popen(kamiCmd, stdin=PIPE, stdout=PIPE, bufsize=1, stderr=STDOUT)    

    # the fuegoToKamiQueue holds the fuego output -- need to push over to kami
    fuegoToKamiQueue = Queue()
    # the kamiToFuegoQueue holds the kami output -- need to push over fuego
    kamiToFuegoQueue = Queue()
    
    # build workers to enqueue and dequeue kami's output
    kamiToQueueWorker = Thread(target=enqueue_output, args=(kamiProc.stdout, kamiToFuegoQueue, "Kami -> Feugo in queue"))
    kamiToQueueWorker.daemon = True

    queueToFuegoWorker = Thread(target=dequeue_output, args=(fuegoProc.stdin, kamiToFuegoQueue, "Fuego in queue -> Fuego"))
    queueToFuegoWorker.daemon = True

    # build workers to enqueue and dequeue fuego's output
    fuegoToQueueWorker = Thread(target=enqueue_output, args=(fuegoProc.stdout, fuegoToKamiQueue, "Fuego -> Kami in queue"))
    fuegoToQueueWorker.daemon = True

    queueToKamiWorker = Thread(target=dequeue_output, args=(kamiProc.stdin, fuegoToKamiQueue, "Kami in queue -> Kami"))
    queueToKamiWorker.daemon = True

    # build worker to enqueue the user's input to kami
    userToQueueWorker = Thread(target=enqueue_output, args=(sys.stdin, fuegoToKamiQueue, "User -> Kami in queue"))
    
    # start up workers
    kamiToQueueWorker.start()
    queueToFuegoWorker.start()
    fuegoToKamiQueue.start()
    queueToKamiWorker.start()
    userToQueueWorker.start()

    kamiProc.wait()
 
    # kami is done -- quit both programs
    kamiProc.kill()
    fuegoProc.kill()

# need as input the backprop file name
argslength = len(sys.argv)
if (argslength != 2):
    print 'Need to supply a neural net source file as arg'
else:
    run_game(sys.argv[1])


