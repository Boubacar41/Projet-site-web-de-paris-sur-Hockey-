from django import forms

RESULT_PREVISION = [('0','Match null'), ('1','Equipe 1'), ('2','Equipe 2')]

class FormPari (forms.Form):
    nom_ut = forms.CharField( required= True, label='Votre nom', max_length =100 )
    choix = forms.ChoiceField ( required= True, label='Votre pronotic', 
    choices= RESULT_PREVISION )
    montant= forms.FloatField( required= True, label= 'Montant en $CAD')