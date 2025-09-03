#!/usr/bin/bash 

# Set the allocation to be charged for this job
# not required if you have set a default allocation
#SBATCH -A edu25.dd2443

# The name of the script is myjob
#SBATCH -J Task3

# The partition
#SBATCH -p main

# 10 minutes wall clock time will be given to this job
#SBATCH -t 00:10:00

# Number of MPI processes
#SBATCH -n 1

#SBATCH --nodes=1

#SBATCH --cpus-per-task=256

java Task3