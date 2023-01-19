# MineFortress mod
## Installation
The project consists of three different projects which need to be in the same workspace.
1. Create some folder and open a command prompt in it
2. Clone the three projects into that folder
```
git clone https://github.com/minefortress-mod/Cardinal-Components-API.git
git clone https://github.com/minefortress-mod/automatone.git
git clone https://github.com/minefortress-mod/minefortress.git
```
3. Rename the `Cardinal-Components-API` folder to `cc-api`
4. Go to folder `cc-api` and run there
```
git checkout 1.18
gradlew assemble
```
5. Go to folder `automatone` and run there
```
git checkout 1.18
gradlew assemble
```
6. Go to folder `minefortress` and open it with IntelliJ IDEA
7. Wait for the project to load with gradle
8. Close IntelliJ IDEA
9. Reopen IntelliJ IDEA
10. Run the `Minecraft Client` configuration
