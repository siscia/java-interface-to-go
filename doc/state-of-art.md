# The State of Art
## of the library till now.

##Dec. 04 2013
--------------
### 22.44
From now I believe I can go ahed without implemeting nothing under clojure.lang.Obj 

I made some progress in the autogenerating of the code, I don't know if it will be really usefull at all, for now however it is being very very useful.

##Dec. 03 2013 
--------------
### 17.36
Pretty sure that is necessary define a common base type, something like java.lang.Object but it has to be very simple, I would dare empty.

I defined the name of the interface "cljgo/interface/NameInterface.go", so much fantasy...

## Dec. 02 2013 
---------------
###Very late at night but still the 02nd
Thinking are arising several issues.

First and again the names, I need to define some kind of standard, thinking about something like: clojurego.NameOfInterface

Then I need to understand what methods I have to implement for every class, golang does not have inheritance, it has composition; however I cannot implement every single methods, embending seems very powerful but I need to understand it better, a good start is [here](http://nathany.com/good/).

```
Patterns used with classical inheritance, like the template method pattern, aren't suitable for embedding. It's far better to think in terms of composition & delegation, as we have here.
```
Pretty much it say that I need to re-define all clojure, big work.

So from what I understand from the article use inheritage is just the wrong way in Go, I need to re-think the whole system completely, I really hoped that that wasn't necessary.

Composition and embending are VERY powerful, I need to think deeply about how to use them.

Basic free thinking flow:

what is the point ?
How to manage difference between OOP and GoLang type system.

In java (OOP) I basically have class that implemets some interfaces or that extends some other class, what that means ?
Implememting an interfaces means that in that class I assure (I sign a contract) that every method defined in the interfcae will be find in the class, and for method I mean all, return type, name, and parameter passed as input.
Extending a class means that I am specialyzing the basic class, however the class I am defining still has all the method of the old class plus some more, some methods may be overwrite. 

In GoLang something different, best reference is [here](http://golang.org/doc/effective_go.html#embedding)

For what I have understand an interface in Go is defined simply by implementing the method of such interfaces, basically it seems more powerful, then I can embend in the struct whatever other type I want, if I don't name it, all the new struct will automatically implement all the old type struct.

Sound wonderful...

Probably is better to take a bottom-up approach; the goal for now is implemeting a simple clojure list.

Not sure anymoer if is a good idea to start from the java interfaces or if it is better to start from the documentation.

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