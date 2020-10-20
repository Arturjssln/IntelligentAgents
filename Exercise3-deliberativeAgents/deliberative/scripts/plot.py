import numpy as np 
import matplotlib.pyplot as plt 



def main():
    number_of_tasks = np.arange(2,8+1)
    steps_bfs = np.array([10, 21, 242, 441, 1710, 15865, 146967])
    time_bfs = np.array([0.003, 0.015, 0.027, 0.045, 0.092, 2.228, 187.613])
    
    steps_ash = np.array([7, 15, 125, 232, 1203, 10905, 103774])
    time_ash = np.array([0.027, 0.076, 0.13, 0.134, 1.982, 4.863, 253.628])
    
    steps_as = np.array([10, 29, 199, 456, 2108, 20458, 191407])
    time_as = np.array([0.019, 0.016, 0.046, 0.079, 0.17, 6.499, 617.841])
    
    fig, ax = plt.subplots(1, 2)
    ax[0].plot(number_of_tasks, steps_bfs, label='BFS')
    ax[1].plot(number_of_tasks, time_bfs, label='BFS')
    ax[0].plot(number_of_tasks, steps_ash, label='A* with heuristic')
    ax[1].plot(number_of_tasks, time_ash, label='A* with heuristic')
    ax[0].plot(number_of_tasks, steps_as, label='A* without heuristic')
    ax[1].plot(number_of_tasks, time_as, label='A* without heuristic')
    ax[0].set_yscale('log')
    ax[1].set_yscale('log')
    ax[0].set_title('Number of steps during planification procedure')
    ax[1].set_title('Time to complete planification procedure')
    ax[0].set_xlabel('Number of tasks')
    ax[1].set_xlabel('Number of tasks')
    ax[0].set_ylabel('Number of step')
    ax[1].set_ylabel('Time [s]')
    ax[0].legend()
    ax[1].legend()
    plt.show()

if __name__ == '__main__':
    main()