i-309051b6

http://ui.opendsp.com/clients/136/advertisers/236/campaigns/313/targeting-strategies/1818

http://ui.opendsp.com/clients/136/advertisers/236/campaigns/313/creatives/2184

9F001EAC46E4F555591B21910234E1EA
9F001EAC54BF3F56F90B705D026D4177

zcard archived:segment:386:fp:236    
203556
zcard archived:segment:387:fp:236
144568



echo SET startsOn_1818 2016-06-10T00:00 | redis-cli -h pace.va.opendsp.com
echo SET endsOn_1818 2016-06-28T00:00 | redis-cli -h pace.va.opendsp.com
echo SET bidPrice_1818 2000 | redis-cli -h pace.va.opendsp.com
echo SET budget_1818 100000000 | redis-cli -h pace.va.opendsp.com

echo SET startsOn_1818 2016-06-10T00:00 | redis-cli -h localhost
echo SET endsOn_1818 2016-06-28T00:00 | redis-cli -h localhost
echo SET bidPrice_1818 2000 | redis-cli -h localhost
echo SET budget_1818 100000000 | redis-cli -h localhost

curl -H 'content-type: application/json' -d @/Users/grisha/mygit/Lot49/WML/pubmatic.json http://local.host:10000/auction/pubmatic