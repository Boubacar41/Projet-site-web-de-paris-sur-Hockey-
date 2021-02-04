from django.http import HttpResponse
from django.http import HttpResponseRedirect
from django.shortcuts import render
from datetime import datetime
from .form import FormPari
import ssl
import requests
import base64
import time
import json 
import glob
import os

def recupListMacths():
    #Recuperation de la liste des matchs  ***************************************************************************************
    url="http://localhost:8080/ListMatchs"
    #payload = "{\r\n \"username\":\"adm_bdembele\",\r\n \"password\":\"Bb75443652-\",\r\n}"
    headers = {
    'Content-Type': "application/json",
    }
    matchs = requests.request("GET", url, headers=headers, verify=False).json()

    listMatchs=[]
        
    for r in matchs:
        match={
            'mId': r['id'] ,
            'team1Id': r['team1Id'],
            'team2Id': r['team2Id'],
            'date': r['date'],
            'ended' : r['ended'],
            'team1Name': r['team1Name'],
            'team2Name': r['team2Name'],
            'scr1': r['score1'],
            'scr2': r['score2'],
            'period' : r['nperiod'],
        } 
        
        listMatchs.append(match)
    return listMatchs
    
#Afficher la page d'acceuil
def home(request):
    listMatchs = recupListMacths()
    context = {'listMatchs' : listMatchs}
    return render (request, 'pages/Index.html', context)

def pariEtat(request):
    return render(request, 'pages/pariSucces.html')
    
