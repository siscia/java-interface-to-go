# The State of Art
## of the library till now.

## Dec. 02 2013
---------------
Given any clojure datastructure the function core.make-file build a go file with describe the complete interface of such file, however there are several issues, first of all the name, I am not sure that the have some sense right now.

Then, and more important, there is no point in define more functions, whit the same name, that return, one a type and another the interface of such type, and so on.

It need to be fixed, basic idea is to build a tree and return the most/least specific type.

Then I also have multiple function with the same name that get different input, I don't think that Go allow that (why not ? weird)  I need to fix that.

Basic idea is to generate an interface that includes all that input so I can pass that... 

What about a function that takes on argument or more then one then ?

Need to think about that.

Also most methodes define right now are methods that are already define importing the interfaces, the method must be only the ones defined strictly in the class.