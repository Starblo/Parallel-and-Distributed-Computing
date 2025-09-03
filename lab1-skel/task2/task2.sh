#!/usr/bin/bash 

# Set the allocation to be charged for this job
# not required if you have set a default allocation
#SBATCH -A edu25.dd2443

# The name of the script is myjob
#SBATCH -J task2

# The partition
#SBATCH -p main

# 20 minutes wall clock time will be given to this job
#SBATCH -t 00:10:00

#SBATCH --nodes=1

# Number of MPI processes
#SBATCH --ntasks=32  

#SBATCH --output=%x_%j.out    # Output file format: jobname_jobid.out

# ml PDC java

# javac MainA.java
# java MainA

# javac MainB.java
# java MainB

javac MainC.java
java MainC


