# covidPredictions

Alternative Approaches for Modelling COVID-19: High-Accuracy Low-Data Predictions

Numerous models have tried to predict the spread of COVID-19.  Manyinvolve myriad assumptions and parameters which cannot be reliably calculated undercurrent conditions.  We describe machine-learning and curve-fitting based models usingfewer assumptions and readily available data

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. 

### Prerequisites

For running the python files, you will need to install pandas, numpy, SciPy and sklearn and seaborn libraries. You may install them via an Anacondo distribution or simply use the following in the project directory (preferrably in a virtualenv)

```
pip install <library name>
```

## Files

* ActiveFromDeath.py, ActiveFromDeathByState.py - Python files, for curve-fitting and predicting the actual number of COVID-19 infections. To run them on updated data and parameters, change the following section in the code:

```python
# SET THESE PARAMETERS ACCORDINGLY

ifr = 0.41
offset = 23
min_death_fact = 1.5
max_death_fact = 4
death_fact = (max_death_fact + min_death_fact)/2

data = pd.read_csv("owid-covid-data.csv") # Data Course: OWID
india_combined = data.loc[data["iso_code"]=="IND",("date","total_cases","total_deaths")]  # For any other country, replace "IND" with respective country code

```

## Miscellaneous Links

National Prediction:https://www.overleaf.com/project/5ea933d883635f0001df191a
All training data from covid19india.org and verification data from worldometer.


CFR: https://www.overleaf.com/read/pfphkdmywshd


Progression: https://www.overleaf.com/6458928194ysdvkmsjscvy


Google Sheets (25 US States) May 25 onwards: https://docs.google.com/spreadsheets/d/19kGvj9H35VDPCbgKrWeobHbZjOqNprcmMWCL_wjtr4E/edit?usp=sharing

Google Sheets made to track CFR fluctuations (compiled till May 10th) : https://docs.google.com/spreadsheets/d/1OijyFvOldjteY_3OFgU1Rr_azDGOhm0Zk2rjbDoKvWE/edit?usp=sharing

A simple back-calculation example : https://www.telegraphindia.com/india/coronavirus-outbreak-what-the-numbers-reveal/cid/1771525#.XrYWo77Qbsg.facebook

All data from https://ourworldindata.org/coronavirus

An article very similar to our CFR paper : https://science.thewire.in/the-sciences/covid-19-pandemic-case-fatality-rate-calculation/


Deaths using Active Cases is surprisingly related using a quadratic with a Rsq of 0.998 ...
