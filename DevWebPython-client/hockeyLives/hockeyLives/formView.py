from django.views.generic import FormView
from django import forms
from django.http import HttpResponse
from django.http import HttpResponseRedirect
from django.shortcuts import render
from datetime import datetime
from .views import recupListMacths
from .form import FormPari
import ssl
import requests
import base64
import time
import json 


class detView(FormView):
    form_class = FormPari
    template_name = "pages/detailsMatch.html"


    #Afficher la page details de match
    def detMatchs(request, id):

        if request . method == 'POST' :

            form = FormPari(request.POST)

            if form.is_valid():

                choice =str(form.cleaned_data ['choix'])
                
                url="http://localhost:8080/Pari"
                payload = "{\r\n \"id\":\""+str(id)+"\",\r\n \"choice\":\""+str(choice)+"\",\r\n \"amount\":\""+str(form.cleaned_data ['montant'])+"\", \r\n \"user\":\""+str(form.cleaned_data ['nom_ut'])+"\"}"
                headers = {
                'Content-Type': "application/json",
                }
                r = requests.request("PUT", url, data=payload, headers=headers, verify=False).json()

                pariResponse = {
                    'status' : r['status'],
                }
            
                context= {'pariResp' : pariResponse}
                #return HttpResponseRedirect ('/pari/')
                return render (request, 'pages/pariSucces.html', context)

        
        else:

            #Recuperation des details du match  ***************************************************************************************
            url="http://localhost:8080/Match"
            
            payload = "{\r\n \"id\":\""+str(id)+"\"}"
            headers = {
            'Content-Type': "application/json",
            }
            r = requests.request("POST", url, data=payload, headers=headers, verify=False).json()

            totPen = int(r['team1Penalties']) + int(r['team2Penalties'])
            det= {
                'date': r['date'],
                'team1Name': r['team1Name'],
                'team2Name': r['team2Name'],
                'scr1': r['team1Goals'],
                'scr2': r['team2Goals'],
                'nbrePen1': r['team1Penalties'],
                'nbrePen2': r['team2Penalties'],
                'tot': totPen,
            } 

            listMatchs = recupListMacths()

            for match in listMatchs:
                if match['mId'] == int(id):
                    mChoice=match

            if (mChoice['ended'] == 1):

                #Recuperation du Bilan des paris du match  ***************************************************************************************
                url="http://localhost:8080/Bilan"
            
                payload = "{\r\n \"id\":\""+str(id)+"\"}"
                headers = {
                'Content-Type': "application/json",
                }
                rep = requests.request("POST", url, data=payload, headers=headers, verify=False).json()

                cmp = 0
                bilan=[]

                for r in rep:
                    if cmp == 0:
                        bilanPari = {
                            'idGame': r['gameId'],
                            'result': r['resFinal'],
                            'sumTB': r['sumTotBet'],
                            'sumTW': r['sumTotWin'],
                            }

                    else:
                        bilanParieur= {
                            'user': r['username'],
                            'choix': r['choice'],
                            'mise': r['bet'],
                            'gain': r['sumWin'],
                            }
                        bilan.append(bilanParieur)

                    cmp=cmp+1 


                context = {'detMatch' : mChoice, 'newdet': det, 'bilanP' : bilanPari, 'bilanU' : bilan}

            else :
                form= FormPari()
                context = {'detMatch' : mChoice, 'newdet': det, 'form' : FormPari}
            
            
            return render (request, 'pages/detailsMatch.html', context)
