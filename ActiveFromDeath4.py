import pandas as pd
import matplotlib.pyplot as plt
import numpy as np
from scipy.optimize import curve_fit
from sklearn.metrics import r2_score


# TODO:

# parameters: CFR (IFR preferable), offset, death_factor

# /////////////////////////////////////////////////////////////////////////// FUNCTIONS LIST:


# //////////////////// Curves

def exponential(x,c0,c1,c2):
    return c0 + c1 * np.exp( c2 * x )

def logistic(x,p,k,r,b):
    return  p*k*np.exp(r*(x-b))/((k-p) + p*np.exp(r*(x-b)))


# //////////////////// Calculates active cases from Deaths Reported

def calcActive(deaths, death_fact, ifr, offset): # back-calculates active from reported deaths
    actual_infected = []
    for i in range(offset, len(deaths)):
        actual_infected.append(deaths[i] * death_fact * (100/ifr))

    actual_infected = np.array(actual_infected)
    return actual_infected


# //////////////////// Functions for curve-fitting

def fit_exp(x, y, offset):

    # optimum parameters for curve-fitting
    p0 = [0.01, 0.08, 0.02]  # initial parameter guess - exponential
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

    new_vals = []

    for i in range(len(x), len(x)+offset):
        new_vals.append(exponential(i, c[0], c[1], c[2]))

    return np.append(actual, new_vals)

def fit_logistic(x, y, offset):

    # optimum parameters for curve-fitting
    p0 = [.05, 0.1, 0.001, .02]  # initial parameter guess - logistic
    c, cov = curve_fit(logistic, x, y, p0)  # curve-fitting - logistic

    print(f"Optimum Parameters are {c}")

    yp = logistic(x, c[0], c[1], c[2], c[3])  # calculate predictions of curve-fit
    print(f"Rsq. of curve fit is {r2_score(y, yp)}")  # Rsq calc.

    # plot
    plt.figure()
    plt.title("Actual Infected Numbers")
    plt.plot(x, y, "r", label="Predicted")
    plt.plot(x, yp, "b", label="Logistic Growth")
    plt.xlabel("Days")
    plt.ylabel("Actual Infected")
    plt.legend()
    plt.show()

    new_vals = []
    for i in range(len(x), len(x)+offset):
        new_vals.append(logistic(i, c[0], c[1], c[2], c[3]))

    return np.append(actual, new_vals)


# ///////////////////////////////////////////////////////////////////////////


ifr = 1.04 # current working estimate for Infection Fatality Rate (1.04 - global ifr estimate)
offset = 23 # from The Lancet (17.8) - to be modified with age structuring, 26 from INDSCI-SIM + 5 day incubation
death_fact = 3.5 # assumption to correlate reported deaths with actual deaths
min_death_fact = 2
max_death_fact = 5

data = pd.read_csv("owid-covid-data.csv")

india_combined = data.loc[data["iso_code"]=="IND",("date","total_cases","total_deaths")]

cases = india_combined["total_cases"].values
deaths = india_combined["total_deaths"].values

# Starts from 1st death reported
for i in range(0,len(deaths)):
    if deaths[i]!=0:
        start_index = i
        break

cases = cases[start_index:]
deaths = deaths[start_index:]

actual = calcActive(deaths, death_fact, ifr, offset)


# ///////////////////////////////////////////////////////////////////////////

# Short Term Forecasting with Curve-Fitting

#prediction = fit_exp(np.array(range(len(actual))), actual, offset)
prediction = fit_logistic(np.array(range(len(actual))), actual, offset)

# /////////////////////////////////////////////////////////////////////////// PLOTS

# Logarithmic Plot

plt.figure()
plt.title("COVID-19 National Projections")

plt.plot(np.array(range(len(cases))), deaths, 'r--', label="Reported Deaths")
plt.plot(np.array(range(len(cases))), cases, label="Confirmed Cases")
plt.plot(np.array(range(len(cases))), deaths * death_fact, color='red', label="Expected Deaths")
plt.fill_between(np.array(range(len(cases))), deaths * max_death_fact, deaths * min_death_fact, alpha = 0.2, color='red', label="Confidence of Expected Deaths")
plt.plot(np.array(range(len(cases))), prediction, color='green', label="Predicted Infections")
plt.fill_between(np.array(range(len(cases))), prediction * (max_death_fact/death_fact), prediction * (min_death_fact/death_fact), alpha = 0.2, color='green', label="Confidence of Predicted Infections")

plt.yscale("log")
plt.xlabel("Day Number")
plt.ylabel("Population (Logarithmic)")
plt.legend()
plt.show()


# Linear Plot

plt.figure()
plt.title("COVID-19 National Projections")

plt.plot(np.array(range(len(cases))), deaths, 'r--', label="Reported Deaths")
plt.plot(np.array(range(len(cases))), cases, label="Confirmed Cases")
plt.plot(np.array(range(len(cases))), deaths * death_fact, color='red', label="Expected Deaths")
plt.fill_between(np.array(range(len(cases))), deaths * max_death_fact, deaths * min_death_fact, alpha = 0.2, color='red', label="Confidence of Expected Deaths")
plt.plot(np.array(range(len(cases))), prediction, color='green', label="Predicted Infections")
plt.fill_between(np.array(range(len(cases))), prediction * (max_death_fact/death_fact), prediction * (min_death_fact/death_fact), alpha = 0.2, color='green', label="Confidence of Predicted Infections")

plt.xlabel("Day Number")
plt.ylabel("Population (Linear)")
plt.legend()
plt.show()