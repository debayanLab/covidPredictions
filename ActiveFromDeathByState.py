import pandas as pd
import matplotlib.pyplot as plt
import numpy as np
from scipy.optimize import curve_fit
from sklearn.metrics import r2_score


# TODO:
# make a 3D array to store state data. Each cell is a state, with 2 series of data - new cases and recovered

# parameters: CFR (IFR preferable), offset

# /////////////////////////////////////////////////////////////////////////// FUNCTIONS LIST:


def exponential(x,c0,c1,c2):
    return c0 + c1 * np.exp( c2 * x )

def logistic(x,p,k,r,b):
    return  p*k*np.exp(r*(x-b))/((k-p) + p*np.exp(r*(x-b)))


def calcActive(data,cfr,offset): # back-calculates active from reported deaths
    data["Actual Infected"] = data["Total confirmed deaths due to COVID-19 (deaths)"]*(100/cfr)
    data["Actual Infected"] = data["Actual Infected"].shift(-offset)


# Functions for curve-fitting

def show_plot(x,y,yp):
    plt.figure()
    plt.title("Actual Infected Numbers")
    plt.plot(x, y, "r", label="Predicted")
    plt.plot(x, yp, "b", label="Exponential")
    plt.xlabel("Days")
    plt.ylabel("Actual Infected")
    plt.legend()
    plt.show()

def show_plot_state(day_num, state_total, state_death, state_active):
    plt.figure()
    plt.title("State Wise")
    plt.plot(day_num, state_total, "b", label="Reported Cases")
    plt.plot(day_num, state_death, "r", label="Reported Deaths")
    plt.plot(day_num, state_active, "b--", label="Actual Infections")
    plt.xlabel("Days")
    plt.ylabel("# of Cases")
    plt.legend()
    plt.show()

def fit_exp(india_combined, x, y):

    # optimum parameters for curve-fitting
    p0 = [0.0001, 0.01, 1.2]  # initial parameter guess - exponential
    c, cov = curve_fit(exponential, x, y, p0)  # curve-fitting - exponential

    print(f"Optimum Parameters are {c}")

    yp = exponential(x, c[0], c[1], c[2])  # calculate predictions of curve-fit
    print(f"Rsq. of curve fit is {r2_score(y, yp)}")  # Rsq calc.

    # plot
    plt.figure()
    plt.title("Actual Infected Numbers")
    plt.plot(x, y, "r", label="Predicted")
    plt.plot(x, yp, "b", label="Exponential")
    plt.xlabel("Days")
    plt.ylabel("Actual Infected")
    plt.legend()
    plt.show()

    for i in range(84, 102):
        india_combined.iloc[i, 3] = exponential(india_combined.iloc[i, 4], c[0], c[1], c[2])

def state_fit_exp( x, y):

    # optimum parameters for curve-fitting
    p0 = [0.0001, 0.001, 0.02]  # initial parameter guess - exponential
    c, cov = curve_fit(exponential, x, y, p0)  # curve-fitting - exponential

    print(f"Optimum Parameters are {c}")

    yp = exponential(x, c[0], c[1], c[2])  # calculate predictions of curve-fit
    print(f"Rsq. of curve fit is {r2_score(y, yp)}")  # Rsq calc.

    # plot
    show_plot(x, y, yp)

    return c



def fit_logistic(india_combined, x, y):

    # optimum parameters for curve-fitting
    p0 = [5, 100, 0.001, 2]  # initial parameter guess - logistic
    c, cov = curve_fit(logistic, x, y, p0)  # curve-fitting - logistic

    print(f"Optimum Parameters are {c}")

    yp = logistic(x, c[0], c[1], c[2], c[3])  # calculate predictions of curve-fit
    print(f"Rsq. of curve fit is {r2_score(y, yp)}")  # Rsq calc.

    # plot
    show_plot(x, y, yp)

    for i in range(84, 102):
        india_combined.iloc[i, 3] = logistic(india_combined.iloc[i, 4], c[0], c[1], c[2], c[3])


# ///////////////////////////////////////////////////////////////////////////


cfr = 3.4 # current working estimate for Case Fatality Rate
offset = 18 # from The Lancet

states = pd.read_csv("state_wise_daily.csv")

state_total = states.loc[states["Status"]=="Confirmed", "TN"].values
state_death = states.loc[states["Status"]=="Deceased", "TN"].values


for i in range(1,len(state_total)):
    state_total[i] += state_total[i-1]
    state_death[i] += state_death[i - 1]

print(state_death)


state_active = []
for i in range(offset,len(state_death)):
    state_active.append(state_death[i]*100/cfr)


for i in range(0,offset):
    state_active.append(None)

state_active = np.array(state_active)

day_num = [None]*(states.shape[0]//3)
for i in range(0,len(day_num)):
    day_num[i] = i+1

day_num = np.array(day_num)


c = state_fit_exp( day_num[0:41], state_active[0:41])

for i in range(len(state_active) - offset, len(state_active)):
    state_active[i] = exponential(i,c[0],c[1],c[2])

show_plot_state(day_num, state_total, state_death, state_active)

