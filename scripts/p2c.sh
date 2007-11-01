#!/bin/bash

# this is a workaround for a bug(?) that VIF_CYCLIC_NORMAL
# doesn't get saved even though --keep-temporary is passed
# as a flag.
t='_';
temp=$1${t};
polymake --keep-temporary $1 VERTICES_IN_FACETS POINTS VERTICES VIF_CYCLIC_NORMAL &> /dev/null;
cp $1 $temp;
polymake $1 VIF_CYCLIC_NORMAL >> $temp;
poly2cpml.py $temp $2
mv $temp $1

# this would work if --keep-temporary would work all the time
#poly2cpml.py $1 $2
