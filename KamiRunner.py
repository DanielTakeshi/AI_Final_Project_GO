#! /usr/bin/env python
import os.path,subprocess, sys
from subprocess import STDOUT,PIPE

# run_game initializes the moves.txt file to contain only the boardsize 9 text -- this is always the first thing passed to fuego
# it then loops on generating the board state for the current moves file, asking kami for its move, generating the resulting board state
# and then asking the user for his move... generating the board state...


def run_game(backpropFileName):
    # clear out moves file
    rmMovesCmd = ['rm', 'moves.txt']
    subprocess.check_call(rmMovesCmd, shell=True)

    # initialize moves file
    touchMovesFile = ['touch', 'moves.txt']
    subprocess.check_call(touchMovesFile, shell=True)
    
    # prepend move to set board to 9x9 to moves file
    with open('moves.txt', 'a') as movesFile:
        movesFile.write('boardsize 9\n')
        
    # call computer gen move, user gen move until done TODO: need ending condition
    while (True):
        print "Generating board state for kami"
        gen_fuego_board_state()
        gen_kami_move(backpropFileName)
        print "Generating board state for user"
        gen_fuego_board_state()
        gen_user_move()

# delete the boardInput file going to fuego -- before we call, we want a clean one
def delete_board_input():
    rmBoardInputCmd = ['rm', 'boardInput.txt']
    subprocess.check_call(rmBoardCommand, shell=True)
    touchBoardFile = ['touch', 'boardInput.txt']
    subprocess.check_call(touchBoardFile, shell=True)

# delete the board file going to kami
def delete_kami_input():
    rmBoardInputCmd = ['rm', 'kamiInput.txt']
    subprocess.check_call(rmBoardCommand, shell=True)
    touchBoardFile = ['touch', 'kamiInput.txt']
    subprocess.check_call(touchBoardFile, shell=True)

# gen fuego board state initializes the boardInput.txt file to be the board state that is
# the result of executing the list of moves in the moves.txt file
# it does this by iterating through the list of moves and passing them to fuego, and then writing
# the result of the showboard command out to file
def gen_fuego_board_state():
    # clean board for incoming output
    delete_board_input()

    # generate the board state for the current moves list
    fuegoCmd = ['feugo']    
    with open("boardInput.txt") as out, open("moves.txt") as movesIn:
        fuegoProc = subprocess.Popen(fuegoCmd, stdin=PIPE, stdout=out, stderr=STDOUT)

        for line in movesIn:
            fuegoProc.stdin.write(line + "\n")
            
        fuegoProc.stdin.write("showboard\n")

        moves_list = [["A", "B", "C", "D", "E", "F", "G", "H", "J"], ["1", "2", "3", "4", "5", "6", "7", "8", "9"]]		
        for x in list(itertools.product(*moves_list)):
            move = x[0] + x[1]
            input_string = "is_legal Black {} \n".format(move)
            fuegoProc.stdin.write(input_string)
        # TODO: Write the legal moves to a file

        fuegoProc.stdin.write("quit\n")
        fuegoProc.stdin.flush()

        fuegoProc.wait()

        fuegoProc.stdin.close()
        fuegoProc.stdout.flush()
        fuegoProc.stdout.close()

# gen_user_move prints out the board state, displays it to the user, and then asks the user for their move
def gen_user_move():
    # generate the board state for a user move -- computer has just gone
    # remove the board output state file
    # display board to the user -- get all lines starting with a digit
    print "Filtering board state for the user"
    output = check_output(["grep", "^[0-9]", "boardInput.txt"])
    print output

    userMove = raw_input("enter your move: ")
    with open("moves.txt", "a") as movesFile:
        movesFile.write(userMove + '\n')
        
# gen_kami_move calls the kami main, passing it the backprop init file.  it assumes that kamiGo will read 
# the board state from kamiInput.txt, and then append the move it would like to make to moves.txt
def gen_kami_move(backpropFileName):
    # generate board for kami -- cleaned version int
    print "Filtering board state for Kami Go"
    output = check_output(["grep", "^[0-9]", "boardInput.txt"])

    # prep board for writing kami board
    delete_kami_input()

    # write file out to kami
    with open("kamiInput.txt") as outFile:
        outFile.write(output)

    print "Passing board state to Kami Go"
    
    # start up kami go and ask it for its move
    kamiCmd = ['java', 'KamiGo', '-d', backpropFileName]
    kamiProc = subprocess.Popen(kamiCmd, stderr=STDOUT)
    
    print "Waiting for Kami Go to make its move"
    kamiProc.wait()
    
    print "Kami Go finished move generation"

# need as input the backprop file name
argslength = len(sys.argv)
if (argslength != 2):
    print 'Need to supply a neural net source file as arg'
else:
    run_game(sys.argv[1])


