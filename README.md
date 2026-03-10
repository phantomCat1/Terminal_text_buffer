# Implementation explanation
Genral idea:\
Have a Cell class representing each cell of the grid\
To make the class less cluttered, a CellAttributes class is used to represent the possible attributes of a cell.\
Going off of the same logic, I chose to create a separate file containing an enum to represent colors. The colors are listed based on the 16  4-bit standard colors of ANSI escape sequences.\
Have a Line class representing each line of the grid. It should make it easier to implement the scrollback part. Instead of having to deal with a 2D grid of cells, I can just have all cells on one line already bundled together and operate on more easily.
Have a TerminalBuffer class that represents the overall/ general terminal buffer with the required operations, cursor, etc.
Finally, have a TerminalBufferTest class and a TestRunner class for unit testing.\

Design decision 1:\
    Imagine a user updates the attributes at a point in time. All cells on the screen that have been written to before this should retain their attributes, but if a user writes to/ inserts text on any line of the screen, the cells should have the newly set attributes. The initial implementation for the Cell class in terms of updating attributes was based on setter methods, i.e. maintaining the same cells, but updating its fields instead of creating new Cell objects. This is due to the fact that creating new objects each time is a more expensive operation than simply updating attributes AND because the buildup of garbage would have caused freezes of the program later. A user is unlikely to update his terminals attributes, so the check for attribute equality should be optimized out by the JVM.
    The consequence is that the code might look a bit uglier. \

Design Decision 2:\
    Line Class could be based on an array or an ArrayList. If the user wouldn't resize the text buffer too often, then an array is perfectly fine.
    If the text buffer gets resized consistently, as simple array would require frequent deep copies, which is quite expensive.
    The Arraylist case can cover both cases if you initialize it with a given size, so you minimize the number of useless internal re-allocations.
    Then, it makes the second scenario less expensive.\

