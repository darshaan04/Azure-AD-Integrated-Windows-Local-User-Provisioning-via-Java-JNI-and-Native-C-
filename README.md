Overview
This project provides a robust Java-based solution for automatically provisioning local Windows user accounts in response to events from Azure Active Directory (Azure AD). It utilizes Java Native Interface (JNI) to bridge Java applications and native C++ code, which interacts directly with Windows NetAPI to manage user accounts. This integration is designed for scenarios where cloud-based identity changes (such as user creation in Azure AD) need to be reflected on on-premises Windows systems automatically.

Features
Automatic User Provisioning: Creates or updates Windows local user accounts in response to Azure AD user events.

JNI Bridge: Leverages Java Native Interface to connect Java code with performant native C++ DLLs.

Windows API Integration: Uses native calls (NetUserAdd, NetUserSetInfo) to ensure compatibility and reliability.

Account Visibility: Ensures created users are visible and enabled in Windows Control Panel.

Secure Handling: Follows best practices for secure credential transmission and privilege elevation.

Cloud-Local Sync: Maps cloud user lifecycle (creation, update) to local accounts in real time.
