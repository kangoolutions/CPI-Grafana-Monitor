# Getting started
Time needed: 30 minutes
## Prerequisits
- You need permissions to create instances for "Process Integration Runtime" with plan "api" on your SAP BTP Subaccount on which your CPI is running
- You need permissions to import packages, deploy artifacts and create security artifacts on cpi. Developer or Admin should work

## Create Grafana Cloud Account
![CleanShot 2024-03-10 at 22 44 01@2x](https://github.com/kangoolutions/CPI-Grafana-Connect/assets/54773577/bb10eec2-8c8d-4351-8e62-4a840e8d13a1)

## Create Folder for Dashboard and import Dashboards from file
![CleanShot 2024-03-10 at 22 59 36@2x](https://github.com/kangoolutions/CPI-Grafana-Connect/assets/54773577/b657850b-69f4-4a07-9e50-62dbec453838)

## Collect Credentials from Grafana Cloud
### Add Access Policies
![CleanShot 2024-03-10 at 23 02 19@2x](https://github.com/kangoolutions/CPI-Grafana-Connect/assets/54773577/338efbdf-4a86-434d-a2da-1640be1c233f)

### Create Token
![CleanShot 2024-03-10 at 23 18 24@2x](https://github.com/kangoolutions/CPI-Grafana-Connect/assets/54773577/29b32293-e4b9-4c9d-ae56-dd8339631eb3)

### Find your Loki username and url
![CleanShot 2024-03-10 at 23 19 24@2x](https://github.com/kangoolutions/CPI-Grafana-Connect/assets/54773577/1101af0c-8838-4a6e-8e7e-72426cf44134)

### Find your Influx username and url
Influx import is used for sending metrics to Grafana Cloud

## Collect Credentials from CPI
### Create Instance on BTP to get access credentials
### Create Service Keys

## Upload Package on CPI, Configure and Deploy
