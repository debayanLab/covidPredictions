import pandas as pd
import matplotlib.pyplot as plt
import numpy as np
from scipy.optimize import curve_fit
from sklearn.metrics import r2_score


# TODO:
# csvToArr(DataFrame) : converts a csv column data into a np array (returns np array)
# calcActive(death,gap) : generates active cases from reported deaths with an offset (time from infected to death)
# currentActiveProjection(active) : fits a curve and predicts current active cases

# parameters: CFR (IFR preferable), offset

# /////////////////////////////////////////////////////////////////////////// FUNCTIONS LIST:


def exponential(x,c0,c1,c2):
    return c0 + c1 * np.exp( c2 * x )

def logistic(x,p,k,r,b):
    return  p*k*np.exp(r*(x-b))/((k-p) + p*np.exp(r*(x-b)))


def calcActive(india_combined,cfr,offset): # back-calculates active from reported deaths
    india_combined["Actual Infected"] = india_combined["Total confirmed deaths due to COVID-19 (deaths)"]*(100/cfr)
    india_combined["Actual Infected"] = india_combined["Actual Infected"].shift(-offset)


# Functions for curve-fitting

def fit_exp(india_combined, x, y):

    # optimum parameters for curve-fitting
    p0 = [0.1, 0.8, 0.2]  # initial parameter guess - exponential
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


def fit_logistic(india_combined, x, y):

    # optimum parameters for curve-fitting
    p0 = [5, 100, 0.001, 2]  # initial parameter guess - logistic
    c, cov = curve_fit(logistic, x, y, p0)  # curve-fitting - logistic

    print(f"Optimum Parameters are {c}")

    yp = logistic(x, c[0], c[1], c[2], c[3])  # calculate predictions of curve-fit
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
        india_combined.iloc[i, 3] = logistic(india_combined.iloc[i, 4], c[0], c[1], c[2], c[3])


# ///////////////////////////////////////////////////////////////////////////


cfr = 3.4 # current working estimate for Case Fatality Rate
offset = 18 # from The Lancet


deaths = pd.read_csv("total-deaths-covid-19.csv")
total = pd.read_csv("total-cases-covid-19.csv")
india_deaths = deaths.loc[deaths["Code"]=="IND",("Date","Total confirmed deaths due to COVID-19 (deaths)")]
india_total = total.loc[total["Code"]=="IND",("Date","Total confirmed cases of COVID-19 (cases)")]
india_deaths["Date"] = pd.to_datetime(india_deaths["Date"])
india_total["Date"] = pd.to_datetime(india_total["Date"])

india_combined = pd.merge(india_total, india_deaths, how="left", on="Date")

calcActive(india_combined,cfr,offset)

day_num = [None]*(india_combined.shape[0])
for i in range(24,len(day_num)):
    day_num[i] = i-23
india_combined["Day Number"] = day_num

india_combined.to_csv("combined-india-covid-19.csv")

# ///////////////////////////////////////////////////////////////////////////

# extract vectors for curve-fitting

x = india_combined["Day Number"].values[24:85]
y = india_combined["Actual Infected"].values[24:85]  # 85 = max Day Number - 18 + 24

fit_exp(india_combined, x, y)
#fit_logistic(india_combined, x, y)

# ///////////////////////////////////////////////////////////////////////////



india_combined.plot(kind='line', x="Date", y=["Total confirmed cases of COVID-19 (cases)","Total confirmed deaths due to COVID-19 (deaths)","Actual Infected"])
plt.show()
