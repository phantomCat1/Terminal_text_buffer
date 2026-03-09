# Implementation explanation
Genral idea:\
Have a Cell class representing each cell of the grid\
To make the class less cluttered, a CellAttributes class is used to represent the possible attributes of a cell.\
Going off of the same logic, I chose to create a separate file containing an enum to represent colors. The colors are listed based on the 16  4-bit standard colors of ANSI escape sequences.\
Have a Line class representing each line of the grid. It should make it easier to implement the scrollback part. Instead of having to deal with a 2D grid of cells, I can just have all cells on one line already bundled together and operate on more easily.
Have a TerminalBuffer class that represents the overall/ general terminal buffer with the required operations, cursor, etc.
Finally, have a TerminalBufferTest class and a TestRunner class for unit testing.