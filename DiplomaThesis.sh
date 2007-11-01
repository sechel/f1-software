#!/bin/bash

JAVA_PROGRAM_DIR="" #in the path

MSG2="Java exec found in "
MSG3="OOPS, this java version is too old "
MSG4="You need to upgrade to JRE 1.5.x or newer from http://java.sun.com"
MSG5="Suitable java version found "
MSG6="Configuring environment..."
MSG7="OOPS, you don't seem to have a valid JRE "
MSG8="OOPS, unable to locate java exec in "
MSG9=" hierarchy"
MSG10="Java exec not found in PATH, starting auto-search..."
MSG11="Java exec found in PATH. Verifying..."


look_for_java()
{
  JAVADIR=$JAVA_HOME
  IFS=$'\n'
  potential_java_dirs=(`ls -1 "$JAVADIR" | sort | tac`)
  IFS=
  for D in "${potential_java_dirs[@]}"; do
    if [[ -d "$JAVADIR/$D" && -x "$JAVADIR/$D/bin/java" ]]; then
      JAVA_PROGRAM_DIR="$JAVADIR/$D/bin/"
      echo $MSG2 $JAVA_PROGRAM_DIR
      if check_version ; then
        return 0
      else
        return 1
      fi
    fi
  done
  echo $MSG8 "${JAVADIR}/" $MSG9 ; echo $MSG4
  return 1
}


check_version()
{
  JAVA_HEADER=`${JAVA_PROGRAM_DIR}java -version 2>&1 | head -n 1`
  JAVA_IMPL=`echo ${JAVA_HEADER} | cut -f1 -d' '`
  if [ "$JAVA_IMPL" = "java" ] ; then
    VERSION=`echo ${JAVA_HEADER} | sed "s/java version \"\(.*\)\"/\1/"`
    if echo $VERSION | grep "^1.[5-6]" ; then
      echo $MSG5 "[${JAVA_PROGRAM_DIR}java = ${VERSION}]" ; echo $MSG6
      return 0	    
    else
      echo $MSG3 "[${JAVA_PROGRAM_DIR}java = ${VERSION}]"
      return 1      
    fi
  else
    echo $MSG7 "[${JAVA_PROGRAM_DIR}java = ${JAVA_IMPL}]" ; echo $MSG4
    return 1
  fi
}


# locate and test the java executable
if [ "$JAVA_PROGRAM_DIR" == "" ]; then
  if ! command -v java &>/dev/null; then
    echo $MSG10
    if ! look_for_java ; then
      exit 1
    fi
  else
    echo $MSG11
    if ! check_version ; then
      if ! look_for_java ; then
        exit 1
      fi
    fi
  fi
fi

# get the app dir
PROGRAM_DIR=`dirname "$0"`
PROGRAM_DIR=`cd "$PROGRAM_DIR"; pwd`

cd ${PROGRAM_DIR}

VM_ARGS=""
if [ "$(uname -m)" == "x86_64" ]; then
	LIB_PATH='native/linux64'
else
	LIB_PATH='native/linux32'
fi	
echo "using $(uname -m) libraries in $LIB_PATH"
MAIN_CLASS='util.DiplomStarter'

for FILE in ./lib/*.jar; do CLASSPATH="${CLASSPATH}:${FILE}"; done
CLASSPATH="$CLASSPATH:./build"

${JAVA_PROGRAM_DIR}java $VM_ARGS -Djava.library.path=$LIB_PATH -cp $CLASSPATH $MAIN_CLASS

