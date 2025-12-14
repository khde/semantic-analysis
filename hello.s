	# A Hello world MIPS SPIM program
	
	.data           # section for data
msg:    
    .asciiz "Hello, World!\n"

    .text           # section for code
    .globl main

main:
    li $v0, 4       # system call for print string
    la $a0, msg     # load address of string msg into $a0
    syscall         # make system call

    li $v0, 10      # system call for exit
    syscall         # make system call
