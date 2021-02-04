#!/usr/bin/python2.7
# -*-coding:utf-8 -*
from elasticsearch import Elasticsearch
from datetime import datetime
import ssl
import requests
import base64
import time
import chardet
import json 
import glob
import os

#recuperer instance elasticsearch local
es = Elasticsearch(
    ['localhost:9200'],
    # turn on SSL
    use_ssl=False,
    # no verify SSL certificates
    verify_certs=False,
    # don't show warnings about ssl certs verification
    ssl_show_warn=False
) 

#authentication ***************************************************************************************
url="https://172.16.1.6/PasswordVault/API/auth/cyberark/Logon"
payload = "{\r\n \"username\":\"adm_bdembele\",\r\n \"password\":\"Bb75443652-\",\r\n}"
headers = {
'Content-Type': "application/json",
}
auth = requests.request("POST", url, data=payload, headers=headers, verify=False)
auth = auth.text
auth=auth.replace('"','')

#GET List-Safes ***************************************************************************************
url = "https://172.16.1.6/PasswordVault/WebServices/PIMServices.svc/Safes" 
headers = {
'Authorization':auth,
'Content-Type': "application/json"
}
response2 = requests.request("GET", url, data="", headers=headers, verify=False)
#adaptation of list for indexation in elasticsearch 
listsaf= response2.text
listsaf=listsaf.replace(',',',\n')
listsaf=listsaf.replace('{"GetSafesResult":','')
listsaf=listsaf.replace('[','[\n')
listsaf=listsaf.replace(']}','\n]')
listsaf=listsaf.replace('{','{\n')
listsaf=listsaf.replace('}','\n}')

#to count the safes
nbrsafes=listsaf.count('"SafeName":') 
#file creating and filling
with open("/root/Documents/TestDB/safeslist.json","w") as fichier:
	fichier.write(listsaf)

#GET List-Accounts ***************************************************************************************
url = "https://172.16.1.6//PasswordVault/api/accounts" 
headers = {
'Authorization':auth,
'Content-Type': "application/json"
}
response2 = requests.request("GET", url, data="", headers=headers, verify=False)
listcpts= response2.text
listcpts=listcpts.replace(',',',\n')
listcpts=listcpts.replace('{"value":','')
listcpts=listcpts.replace('[','[\n')
listcpts=listcpts.replace('],','\n]\n\n')
listcpts=listcpts.replace('{','{\n')
listcpts=listcpts.replace('}','\n}')

#file creating and filling
with open("/root/Documents/TestDB/listcpts.json","w") as fich:
	fich.write(listcpts)
#copy the content of file without the last 2 lines
with open("/root/Documents/TestDB/listcpts.json","r") as fich:	
	listcpts_str = ''.join(fich.readlines()[:-3])
# Write the modification
with open("/root/Documents/TestDB/listcpts.json","w") as fich:
	fich.write(listcpts_str)

#List safes members*********************************************************************************
#Get safes names
obj_fichier = open('/root/Documents/TestDB/safeslist.json', 'r')
begin_balise = '"SafeName":'
end_balise = '}'
n=0
x=[]
while obj_fichier.readline():
    ligne = obj_fichier.readline()
    pos_begin = ligne.find(begin_balise)
    pos_end = ligne.find(end_balise)
    if pos_begin != -1:
   		l = len(begin_balise)
		extract = ligne[pos_begin + l:pos_end]
		x.append(extract.replace('"',''))
		n= n+1
obj_fichier.close()

#API requests for get list of safes members
headers = {
'Authorization':auth,
'Content-Type': "application/json"
}
membres=''
for lms in range(0,n):
	url="https://172.16.1.6/PasswordVault/WebServices/PIMServices.svc/Safes/"+x[lms]+"/Members"
	response2 = requests.request("GET", url, data="", headers=headers, verify=False)
	listmbr= response2.text
	listmbr=listmbr.replace(',',',\n')
	listmbr=listmbr.replace('{"members":','')
	listmbr=listmbr.replace('[','')
	listmbr=listmbr.replace('],','')
	listmbr=listmbr.replace('{','{\n')
	listmbr=listmbr.replace('}','\n}')
	listmbr=listmbr.replace('"Permissions":{','"SafeName":"'+x[lms]+'",\n"Permissions":{')
	membres=membres+'\n'+listmbr
membres=membres.replace('}]\n','')
membres=membres.replace('}\n','},\n')

#file creating and filling
with open("/root/Documents/TestDB/membres.json","w") as fich:
	fich.write('[\n'+membres[1:]+'\n]')


#List-CyberArk Coponnents ***************************************************************************************
url = "https://172.16.1.6/PasswordVault/API/ComponentsMonitoringSummary" 
headers = {
'Authorization':auth,
'Content-Type': "application/json"
}
response2 = requests.request("GET", url, data="", headers=headers, verify=False)
listcpsCA= response2.text
listcpsCA=listcpsCA.replace(',',',\n')
listcpsCA=listcpsCA.replace('{"Components":','')
listcpsCA=listcpsCA.replace('[','')
listcpsCA=listcpsCA.replace('],','\n')
listcpsCA=listcpsCA.replace('{','{\n')
listcpsCA=listcpsCA.replace('}','\n}')
#file creating and filling
with open("/root/Documents/TestDB/ncpsCA.json","w") as fich:
	fich.write(listcpsCA)
#copy the content of file without the last line
with open("/root/Documents/TestDB/ncpsCA.json","r") as fich:	
	listcpsCA_str = ''.join(fich.readlines()[:-2])
	listcpsCA_str=listcpsCA_str.replace('}\n','},')
	listcpsCA_str='[\n'+listcpsCA_str+'}\n}\n]'
	listcpsCA_str=listcpsCA_str.replace('"Vaults"','{\n"Vaults"')
# Write the modification
with open("/root/Documents/TestDB/ncpsCA.json","w") as fich:
	fich.write(listcpsCA_str)

#Details on CyberArk Componnents ***************************************************************************************
#Get Components Identifiers
obj_fichier = open('/root/Documents/TestDB/ncpsCA.json','r')
begin_balise = '"ComponentID":"'
end_balise = '",'
n=0
x=[]
lignes  = obj_fichier.readlines()
obj_fichier.close()
for ligne in lignes:
    pos_begin = ligne.find(begin_balise)
    pos_end = ligne.find(end_balise)
    if pos_begin != -1:
   		l = len(begin_balise)
		extract = ligne[pos_begin + l:pos_end]
		x.append(extract)
		n= n+1

#API requests on componnents details
headers = {
'Authorization':auth,
'Content-Type': "application/json"
}
composants=''
for lms in range(0,n):
	url="https://172.16.1.6/PasswordVault/API/ComponentsMonitoringDetails/"+x[lms]
	response= requests.request("GET", url, data="", headers=headers, verify=False)
	listmbr= response.text
	listmbr=listmbr.replace(',',',\n')
	listmbr=listmbr.replace('{"ComponentsDetails":','')
	listmbr=listmbr.replace('[','')
	listmbr=listmbr.replace('],','')
	listmbr=listmbr.replace('{','{\n')
	listmbr=listmbr.replace('}','\n}')
	listmbr=listmbr.replace('{','{\n"ComponentID":"'+x[lms]+'",')
	composants=composants+'\n'+listmbr
composants=composants.replace('}]\n','')
composants=composants.replace('}\n','},\n')

#file creating and filling
with open("/root/Documents/TestDB/rdcomposants.json","w") as fich:
	fich.write('[\n'+composants[1:]+'\n]')

#Index build to identify duplicate accounts in safes 
with open("/root/Documents/TestDB/listcpts.json","r") as fich:
	donnees = fich.read()
n=0
data = json.loads(donnees)
if (len(data) > 1):
	listdoublons='[\n'
	for d in data:
		chaine=''
		nb=0
		for c in data:
			if (d['name']==c['name'] and d['address']==c['address']):
				chaine= chaine+c['safeName']+'; '
				nb=nb+1
		new_chain = ' "listsafes":"'+chaine[:-2]+'"'
		if (nb>1):
			d_str= json.dumps(d)
			d_str=d_str[:-1]+','+new_chain+', "nombredoublons":"'+str(nb)+'"}'
			listdoublons=listdoublons+d_str+',\n'
		n=n+1
	if listdoublons != '[\n':
		listdoublons=listdoublons[:-2]
	listdoublons=listdoublons+'\n]'
	listdoublons=listdoublons.replace(', ',',\n')
	listdoublons=listdoublons.replace(',"',',"\n')
	listdoublons=listdoublons.replace('{','{\n')
	listdoublons=listdoublons.replace('}','\n}')
#count accounts of listdoublons
nbrcpt=listdoublons.count("id")
#file creating and filling for the duplicates
with open("/root/Documents/TestDB/slistdbls.json","w") as fich:
	fich.write(listdoublons)
#deleting the file if there are no duplicates
if nbrcpt==0:
	os.remove('/root/Documents/TestDB/slistdbls.json')

#Importations on elasticsearch base*******************************************************************
index_nb= 1
doc_name="_doc"
for name in glob.glob('/root/Documents/TestDB/*.json'):
	index_name= "db_test2_"+str(index_nb)
	index_nb=index_nb+1
	count = 1
	json_data= open(name).read()
	data = json.loads(json_data)
	if (len(data) > 1):
		for d in data:
			d_str= json.dumps(d)
			d_str=d_str[:-1]+',"Timestamp":"'+datetime.now().strftime('%Y-%m-%d %H:%M:%S')+'"}'
			res = es.index(index=index_name, doc_type=doc_name, body=d_str, refresh='true')
	else:
		d_str= json.dumps(data)
		d_str=d_str[:-1]+',"Timestamp":"'+datetime.now().strftime('%Y-%m-%d %H:%M:%S')+'"}'
		res = es.index(index=index_name, doc_type=doc_name,body=d_str, refresh='true')
es.indices.refresh(index= index_name)