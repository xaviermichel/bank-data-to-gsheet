const fs = require('fs');
const path = require('path');
const { setTimeout } = require('node:timers/promises');

async function waitForFileToDownload(downloadPath) {
    console.log('Waiting to download file...');
    let filename;
    while (!filename || filename.endsWith('.crdownload')) {
        filename = fs.readdirSync(downloadPath)[0];
        await setTimeout(2000);
    }
    return filename;
}

async function download(page, selector) {
    const downloadPath = path.resolve(__dirname, 'data');
    console.log(`Downloading file to : ${downloadPath}`);
    await page._client().send('Page.setDownloadBehavior', { behavior: 'allow', downloadPath: downloadPath });
    await setTimeout(5000);
    await page.click(selector);
    let filename = await waitForFileToDownload(downloadPath);
    return path.resolve(downloadPath, filename);
}

module.exports.waitForFileToDownload = waitForFileToDownload;
module.exports.download = download;

