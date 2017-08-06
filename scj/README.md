# scj - SCI (Simple Console Interface) for Jem
Console tool for processing e-books.

## Required
* jem-epm
* jem-kotlin
* jem-formats
* jem-crawler
* mala-cli

## Command line examples
Convert UMD book to ePub and set new title, author

    scj input.umd -c -t epub -o output.epub -atitle=Example -a author=PW

View attributes of UMD book and its chapter

    scj input.umd -w title -w publisher -w #1$title

View table of contents (TOC) of ePub book

    scj input.epub -w toc

More example see the doc of scj.
