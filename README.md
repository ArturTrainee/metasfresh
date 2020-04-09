# <img src='/images/metasfresh_logo_green.jpg' height='60' alt='metasfresh Logo' aria-label='metasfresh.com' /></a>

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/30ecd0d9ba8a4561a60335644b592418)](https://www.codacy.com/gh/metasfresh/metasfresh?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=metasfresh/metasfresh&amp;utm_campaign=Badge_Grade)
[![release](https://img.shields.io/badge/release-5.144-blue.svg)](https://github.com/metasfresh/metasfresh/releases/tag/5.144)
[![Gitter](https://img.shields.io/gitter/room/nwjs/nw.js.svg)](https://gitter.im/metasfresh)
[![Krihelimeter](http://krihelinator.xyz/badge/metasfresh/metasfresh)](http://krihelinator.xyz)
[![license](https://img.shields.io/badge/license-GPL-blue.svg)](https://github.com/metasfresh/metasfresh/blob/master/LICENSE.md)

![Twitter Follow](https://img.shields.io/twitter/follow/metasfresh?style=social)


metasfresh is a responsive, Free and Open Source ERP System. Our aim is to create fast and easy-to-use enterprise software with an outstanding user experience.

> **__Competitive ERP is Free__**

Equipped with wide and detailed functionality, metasfresh fits for companies from industry and trade that are searching business software that provides high scalability and flexibility.

It has a 3-tier architecture with Rest-API and a Web User Frontend developed in HTML5/ ReactJS/ Redux.


![metasfresh-sales-order](https://user-images.githubusercontent.com/13365687/36896187-f5ed2e48-1e11-11e8-9c41-a7878c148f81.gif)

<img src="/images/screenshot-kpi-dashboard.png" width="33%" alt="KPI Dashboard"></img> <img src="/images/screenshot-sales-order.png" width="33%" alt="Sales Order Window"></img> <img src="/images/screenshot-material-receipt.png" width="33%" alt="Material Receipt Window"></img>

## Installation
We publish a stable Release of metasfresh each Friday - ok, we skip 1 week at the end of the year ;) . It can be downloaded [here](http://metasfresh.com/en/download/).

metasfresh can be installed via **Docker** or **Ubuntu Installer**

**Docker** [How do I setup the metasfresh stack using Docker?](http://docs.metasfresh.org/howto_collection/EN/How_do_I_setup_the_metasfresh_stack_using_Docker.html)

**Ubuntu** [How do I install metasfresh using the Installation package?](http://docs.metasfresh.org/installation_collection/EN/installer_how_do_install_metasfresh_package.html)

>**First steps:**
>- [How do I log on?](http://docs.metasfresh.org/webui_collection/EN/Logon.html)
>- [How do I change the Interface Language?](http://docs.metasfresh.org/webui_collection/EN/SwitchLanguage)
>- [How do I Setup my Company?](http://docs.metasfresh.org/webui_collection/EN/InitialSetupWizard)
>- [How do I create my first Sales Order?](http://docs.metasfresh.org/webui_collection/EN/SalesOrder_recording)
>- [How do I create my first Shipment?](http://docs.metasfresh.org/webui_collection/EN/Ship_SalesOrder)
>- [How do I create my first Invoice?](http://docs.metasfresh.org/webui_collection/EN/Invoice_SalesOrder)

## Documentation
If you are new to metasfresh and would like to learn more, then you can find our documentation here:

- [Admins](http://docs.metasfresh.org/pages/installation/index_en)
- [Users](http://docs.metasfresh.org/pages/webui/index_en)
- [Developers](http://docs.metasfresh.org/index.html)
- [Tester](http://docs.metasfresh.org/pages/tests/index_en)

## Discussion
Join one of the gitter rooms [metasfresh](https://gitter.im/metasfresh/metasfresh), [metasfresh-webui-frontend](https://gitter.im/metasfresh/metasfresh-webui-frontend), [metasfresh-documentation](https://gitter.im/metasfresh/metasfresh-documentation) or visit us in our [forum](https://forum.metasfresh.org/).

## Contributing
Do you want to help improving documentation, contribute some code or participate in functional requirements. That's great, you're welcome! Please read our [Code of Coduct](https://github.com/metasfresh/metasfresh/blob/master/CODE_OF_CONDUCT.md) and [Contibutor Guidelines](https://github.com/metasfresh/metasfresh/blob/master/CONTRIBUTING.md) first.

### "Monorepo"
To check out only certain parts of this repository, we recomment to get git version 2.25.0 or later and use the [git-sparse-checkout](https://www.git-scm.com/docs/git-sparse-checkout) feature.
Examples:
* to get started, do `git sparse-checkout init --cone`
  * this will leave you with just the files in the repo's root folder, such as the file you are reading
* to get the frontend code, do `git sparse-checkout set frontend`
* to go back to having everything checked out, do `git sparse-checkout disable`

## What's new in metasfresh ERP?
If you are interested in latest improvements or bug fixes of metasfresh ERP, then take a look in our [Release Notes](https://github.com/metasfresh/metasfresh/blob/master/ReleaseNotes.md).
