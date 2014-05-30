MassDataGenerator
=================

Overview
---------

The MassDataGenerator is a simple tool to generate a custom amount of data on the basis of an input file.
An input file with any desired data is reading and put as often specified in the output file.
The output can be customized with pattern. There is a pattern for random UUIDs, a numberic sequenz and the current date.


Options
--------

| Short Option | Long Option  | Has Arguments | Description                  |
| :----------: | ------------ | :-----------: | ---------------------------- |
| __-c__       | --count      | *Yes*         | The amount of generatet data |
| __-d__       | --dateFormat |	*Yes*         | A custom date format         |
| __-h__       | --help       | *No*          |	Show the help                |
| __-i__       | --input      |	*Yes*         | The input file               |
| __-o__       | --output     | *Yes*         | The output file              |


Pattern
--------

| Pattern   | Description                            |
| --------- | -------------------------------------- |
| \#UUID\#  | A random UUID                          |
| \#SEQ\#   | A consecutive number (starting with 1) |
| \#DATE\#  | The current date (*YYYY-MM-d  h:_m_:s.S*)   |
