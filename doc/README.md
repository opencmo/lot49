# Logic flows

 * [Bidder startup](./bidder-startup.png)
 * [Bid request](./bid-request.png)
 * [Ad eligibility, first pass](./ad-eligibility-first-pass.png)
 * [Get bid subflow](./get-bid-subflow.png)
 * [Tag eligibility subflow](./tag-eligibility-subflow.png)
 * [Ad cache refresh](./ad-cache-run.png) - runs on schedule, every minute usually (configurable).
 * [Bid candidate manager run](./bid-candidate-manager-run.png) - recurs until all bid candidates either pass, all fail, or timeout occurs. 
 * [Lost auction task](./lost-auction-task.png) - scheduled to run a configurable amount of time after bid has been submitted. Will be canceled if a win/loss/error received from the exchange. Otherwise will run.
 * Flow if [NUrl required](./nurl-required.png). For discussion of NUrl, see:
    * TBD Javadoc
    * TBD OpenRTB
 * [Impression received](./impression.png)
 * []()