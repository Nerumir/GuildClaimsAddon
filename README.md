# Plugin features :

*This plugin do not need any other claiming plugin. Only depedency is **[Guilds](https://glare.dev)**.*

- A **radius** that depend on the guild tier.
- An **expiration date** in days (since last time the guild master logged out). Past that, it will remove the guild's claim.
- Ability to disable all kind of **explosions** in claim.
- Ability to allow the use of **creeper eggs** in any claim (for factions servers for example). That's something **worldguard can't do**.
- **Farewell** and **greeting** messages.
- Don't use any plugin other than **Guilds**.
- A **bypass** command with a permission for it.
- Possibility to decide in **which world** we can claim (with a **blacklist** or **whitelist** system, it's more convenient)
- **YAML** storage or **MySQL** storage (fully able to handle **multiple servers** architecture if MySQL storage is choosen)
- **Overriding** your /g claim and /g unclaim commands and automatically reload with the /g reload command after Guilds does.
- Claims works with roleperms of **roles.yml of Guilds folder** for interact, destroy and place.
- Members are **immuned to PvP** damages when standing in their claim.

**GriefDefender** will not be as simple as that for players to use and a **guild is unable to have claims**, it will be bind to a player so it just simply **cannot work** with Guilds. The only thing GriefDefender does is giving the possibility for players to trust guilds members in their claims, but it **doesn't provide a claiming system for Guilds**.

Last thing i want to say is, as it is an addon that works by **overriding existing Guilds commands**, it will be a **much better solutions** than forks because it will follow **Guilds updates** quite easily.

## Initial config.yml

```yml
####################################################
#            ONLY THE LEADER CAN CLAIM !           #
#                                                  #
#      Reloading Guilds will reload this addon     #
#  The claiming commands are the ones from Guilds  #
#       Admin claiming commands will not work      #
#                                                  #
#       Only one additionnal command exists        #
#      It's /cbp, to bypass claim protections      #
#   Permission for that command is guilds.bypass   #
#                                                  #
#      This is better to disable claims in         #
#    your guilds config file to use this addon     #
####################################################


#This only works with MySQL databases.
sql:
  #If sql is not enabled, datas of the claims will be stored in the claims.yml file.
  #Connection infos are in the config.yml file of Guilds.
  enabled: false

#A string that is an identifier of the server. It will be used only if MySQL is used to store claims, it's to avoid conflicts when working with bungee, making it bungee compatible.
serverMarker: 'server1'

#List of worlds where the players can claim. If blacklist is set to true, then, it's the list of worlds where player cannot claim.
worlds: ['world']
blacklist: false

#####################################################

#In a claim, interactions (like opening chests, pressing buttons, etc..), placing blocks and breaking blocks are things that only players in the guild of the claim can perform.
#They can interact,break,place according to the permission of the roles in the roles.yml file of your Guilds folder.
#The last thing that changes with claims is PvP. A guild member that is in his claim he immuned to all kind of PvP damages, but he can still damage other players !

#####################################################

#if true, everyone can spawn a creeper egg in any claim
creeper-eggs: true
#if true, all kind of explosions are enabled on any claim (making TNT canons possibles for example), if false, every explosion on a claim will be canceled.
explosions-on-claims: true

#Radius of the claim for each tier of the guild.
#Expiration is the number of days of inactivity of the leader of the guild (last time logged in) before a claim is removed (permanently).
#Make sure the key matches the tier id in the tier.yml file of your Guilds's folder.
tiers:
  '1':
    radius: 50
    expiration: 15
  '2':
    radius: 70
    expiration: 30
  '3':
    radius: 100
    expiration: 45
  '4':
    radius: 150
    expiration: 45
  '5':
    radius: 200
    expiration: 45
  '6':
    radius: 350
    expiration: 90

#messages shown by the addon in game. Use '&' for minecraft colors.
messages:
  bypass-on: '&aBypass mode for claims activated.'
  bypass-off: '&cBypass mode for claims desactivated.'
  no-console: '&4This command cannot by executed by console.'
  not-in-guild: '&cYou are not in a guild.'
  not-in-valid-world: '&cYou are not in a valid claiming world.'
  not-leader: '&cYou are not the leader of the guild.'
  claim-exists: '&cA claim already exists.'
  claim-success: '&aClaim successfuly created.'
  claim-overlap: '&cCannot claim, it will overlap other claims.'
  claim-not-exist: '&cYour guild does''nt have any claim.'
  unclaim-success: '&aClaim successfuly removed.'
  claim-not-on-server: '&cThe claim of your guild is not on this server ! You need to go to the server it is on to unclaim or remove your guild !'
  entering-claim: '&eYou entered the territory of the guild &a{name} &e!'
  leaving-claim: '&eYou left the territory of the guild &a{name} &e!'
  cannot-do-that: '&cYou can''t do that here, it''s &6{name}&c''s territory !'
```