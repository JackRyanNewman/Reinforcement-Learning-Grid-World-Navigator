# Reinforcement Learning Grid World Navigator

A Java implementation of SARSA and Q-learning algorithms for training an agent to navigate through complex grid world environments. The system learns optimal policies to reach goals while avoiding hazards like mines and cliffs.

## Features

- **Dual Algorithm Support**: Both SARSA (on-policy) and Q-learning (off-policy) implementations
- **Complex Environment Modeling**: Handles probabilistic movement with drift, multiple cell types, and stochastic transitions
- **Adaptive Learning**: Decaying learning rate and exploration parameters for convergence
- **Comprehensive Evaluation**: Multiple verbosity levels for detailed analysis of learning progress
- **Advanced Options**: Eligibility traces and feature-based Q-learning for improved performance

## Command Line Arguments

- `-f <filename>`: Environment configuration file (required)
- `-a <value>`: Initial learning rate α (default: 0.9)
- `-e <value>`: Initial exploration rate ε (default: 0.9)
- `-g <value>`: Discount factor γ (default: 0.9)
- `-na <value>`: Learning rate decay episodes (default: 1000)
- `-ne <value>`: Exploration rate decay episodes (default: 200)
- `-p <value>`: Action success probability (default: 0.8)
- `-q`: Use Q-learning instead of SARSA
- `-T <value>`: Number of learning episodes (default: 10000)
- `-u`: Enable Unicode character output
- `-v <level>`: Verbosity level 1-4 (default: 1)
- `-l <value>`: Eligibility trace parameter λ 
- `-w`: Use feature-based Q-learning
- `-p`: pretty print version of the grid worldd. 

## Environment Details

The grid world contains multiple cell types with distinct behaviors:
- **Start (S)**: Initial agent position
- **Goal (G)**: Terminal state with positive reward
- **Mine (M)**: Terminal state with large negative reward  
- **Cliff (C)**: Returns agent to start with negative reward
- **Block (B)**: Impassable obstacle
- **Empty (-)**: Standard navigable cells

Movement incorporates realistic uncertainty - actions may succeed as intended or drift perpendicularly, simulating real-world navigation challenges.

## Output

The system provides comprehensive visualization and analysis:
- Learned policy display with ASCII or Unicode arrows
- Q-value tables showing state-action valuations
- Learning progression metrics during training
- Parameter decay tracking and final performance evaluation

## Read 00-References\assignment-04.pdf for more info

## Building and Running

Compile the Java source files and run from command line with desired parameters:
```bash
javac *.java
java Driver -f environment.txt -v 2 -q -T 5000