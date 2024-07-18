| [README](README.md) | [Whats new?](whats_new.md) | [Getting Started](getting_started.md) | [How to use](how_to_use.md) | [Contribute](contribute.md) |     |
| ------------------- | -------------------------- | ------------------------------------- | --------------------------- | --------------------------- | --- |

# Getting started and initial setup

Time needed: 30 minutes

# Prerequisits

- You need permission to create instances for "Process Integration Runtime" with plan "api" on your SAP BTP Subaccount on which your CPI is running
- You need permission to import packages, deploy artifacts, and create security artifacts on cpi. Developer or Admin should work

# Steps

1. Create a Grafana Cloud Account
2. Create a Folder and import dashboards in Grafana Cloud
3. Collect credentials and urls from Grafana Cloud
4. Import Integration Flow to SAP Cloud Integration
5. Create api instance on BTP and collect credentials
6. Create credentials artifacts on Cloud Integration and configure Integration Flow
7. Configure the log and metric collecting Integration Flow and deploy
8. Have fun :-)
9. Give us some feedback

## 1. Create a Grafana Cloud Account

The Grafana Cloud Account is free in its basic version for up to three users and a sufficient number of logs and metrics for this project. Be aware that retention time is only 14 days.

1. Go to [www.grafana.com](https://grafana.com/auth/sign-up/create-user) and create a new account.

   ![Grafana Free Forever](res/media/screenshots/grafana.com/grafana.com_free_forever.png)

There are two different parts to your new account. Your Grafana Instance where you see dashboards and the Account Management.

### URL Structure

**Grafana Instance**: https://yourgrafanatenantname.grafana.net
Used to see/explore your data and dashboards

Looks like this:

![Grafana Instance](res/media/screenshots/grafana/grafana_instance.png)

**Account Management**: https://grafana.com/orgs/yourgrafanatenantname
Used to get credentials and invite users

Looks like this:

![Account Management](res/media/screenshots/grafana/grafana_account_management.png)

## 2. Create a Folder for the Dashboard and import Dashboards from the File

1. In your Grafana Instance (https://yourgrafanatenantname.grafana.net), go to the dashboards and create a new folder for the new dashboards that we will import. It is called "Kangoolutions Cloud Integration Monitor".

   ![Create Folder](res/media/screenshots/grafana.com/create_folder.gif)

2. Import the dashboards by clicking Import -> Dashboard. You can use IDs 20662 and 20742 (to import from the Grafana repository) or download and import the dashboards as json from the main branch --> Grafana Dashboards.
   During import, you need to choose the Data Sources from the dropdown (...-logs and ...-prom).

## 3. Collect Credentials from Grafana Cloud

To send logs and metrics to Grafana Cloud, we need to collect/create the following data.

- A. Access Policy with access token to logs and metrics

**Grafana Loki**

- B. Username
- C. URL

**Influx (for importing metrics)**

- D. Username
- E. URL

**CPI API**

- F. ClientID
- G. ClientSecret
- H. TokenUrl
- I. CPI URL

**Open Telemetry (in case you want to use traces)**

- J. Open Telemetry InstanceID
- K. OpenTelemetry URL

It might be helpful to copy this list and add it together with the credentials/urls to an editor/notepad

You need to do this in your Account Management (https://grafana.com/orgs/yourgrafanatenantname)

1. First we need to create an access policy artifact + create the first token. You can do this in the account management.
   Click on Security --> Access Policies --> Create Access Policy.
   It is important, that you give write access to logs, metrics, and traces (not yet used but maybe later)

   ![Access Policy](res/media/screenshots/grafana.com/create_access_policy.gif)

2. Create a token by clicking "Add token" on your new access policy. Remember the created token. We will reference it with (A)

   ![Create Token](res/media/screenshots/grafana.com/create_token.gif)

3. Now we need to get the user and URL for Loki. To get this, go back to the account management page and click on Loki. Here you can see username (B) and loki url (C).

   ![Loki Credentials](res/media/screenshots/grafana.com/loki_username_url.gif)

4. The next part is the URL and username for metrics. Again, in the account management, click Influx and note username (D) and url (E).

   ![Influx](res/media/screenshots/grafana.com/influx_metrics_username_url.gif)

5. To get Open Telemetry Access Data, click Open Telemetry and copy endpoint and instance id (like: https://otlp-gateway-prod-us-central-0.grafana.net/otlp)

## 4. Import Integration Flow to SAP Cloud Integration

Download the package from the git repository (main branch --> CPI --> Artifacts --> "Send Message Logs and Metrics to Grafana.zip") and import it to your Cloud Integration Tenant. We recommend creating a new package "Kangoolutions Grafana Monitoring" first.

![Package](res/media/screenshots/cpi/cpi_package.png)

## 5. Create api instance on BTP and collect credentials

1. To read message logs etc. from Cloud Integration, you need to have access to the underlying BTP tenant and subaccount (the one where your Cloud Integration runs.). Please activate an instance with the name "Process Integration Runtime" with plan "api" on your BTP subaccount.

Do it by following the steps:

1. Go to your subaccount --> Services --> Instances and Subscriptions --> create a new instance of "Process Integration Runtime" with "api" plan, and then select role MonitorDataRead. Select "Client Credentials" as grant type.

   ![Create Instance](res/media/screenshots/cpi/cpi_create_instance.gif)

2. Create a Service Key from the created instance.
   Choose "ClientId/Secret" as the Key Type.

   ![Create Credentials](res/media/screenshots/cpi/create_api_key.gif)

3. Show key and note clientid (F), clientsecret (G), tokenurl (H) and url (I).

   ![Show Key](res/media/screenshots/cpi/show_key.gif)

## 6. Create credentials artifacts on Cloud Integration and configure Integration Flow

Now we need to connect the Integration Flow from the package in step 4 with the Grafana Cloud account. Check the reference to the list in step 3.

1. Create a User Credentials artifact with the Loki credentials (Username (B) and Token (A)). Remember the name of the artifact. We recommend "grafana-loki".
1. Create a User Credentials artifact with the Influx credentials (Username (D) and Token (A)). Remember the name of the artifact. We recommend "grafana-influx".
1. Create an OAuth 2 Client Credentials artifact with clientid (F), clientsecret (G) and token url (H) from 5. Remember the name of the artifact. We recommend "cpi-api".
1. Create a User Credentials artifact with the Open Telemetry credentials (Username (J) and Token (A)). Remember the name of the artifact. We recommend "otel".

## 7. Configure the log and metric collecting Integration Flow and deploy

The last step is to configure the Integration Flow "Send MessageLogs and Metrics to Grafana" in the package "Kangoolutions Grafana Monitor" from step 4.

Please check if the following values are configured:
| Field | Example Value | Description |
|------------------------------------------------------|-----------------------------------------------------------|---------------------------------------|
| Receiver -> InfluxMetrics -> influx metrics base url | https://influx-prod-10-prod-us-central-0.grafana.net | The url for the influx import |
| Receiver -> InfluxMetrics -> Credential Name | grafana-influx | The credentials for the influx import |
| Receiver -> Loki -> loki base url | https://logs-prod3.grafana.net | The loki url |
| Receiver -> Loki -> Credential Name | grafana-loki | The credentials for loki |
| Receiver -> CPIInternal -> cpi base url | https://xxxxx.it-cpi019.cfapps.us10-002.hana.ondemand.com | From instance service key (I) |
| Receiver -> CPIInternal -> Credential Name | cpi-api | Name of the OAuth2 Client Credentials artifact for the CPI Instance in 6.3 |
| More -> 'stage like dev, quality or prod' | dev | The stage of your tenant |
| More -> 'fallback last run datetime' | now | The time at which it should start to collect entries. Normally keep "now" |
| More -> 'regex to exclude integration flows' | (MyIflowToExclude|OtherIflowtoExclude) | Regex for excluded IFlows |
| More -> 'send traces via open telemetry' | true or false | Check [How to use](how_to_use.md) |
| More -> 'tenant name' | mytenantname | Tenant name as identifier in Grafana (like start of url) |
| More -> 'time zone' | CET | your time zone for logs. [Reference](https://code2care.org/pages/java-timezone-list-utc-gmt-offset/) |

## 8. Run it.

Congratulations! If you go back to your dashboards in Grafana Cloud, you will hopefully now see data.

![Dashboard](res/media/screenshots/promotion1.png)

You can connect other Cloud Integrations if you want. Just repeat steps 3 to 6 for each instance.

## 9. Give us Feedback (and Collaborate?)

It would be nice if you could give us some feedback, on how you use our tool. You can send us a message via [LinkedIn](https://www.linkedin.com/in/dominic-beckbauer-515894188/), GitHub issue etc. to do so.
